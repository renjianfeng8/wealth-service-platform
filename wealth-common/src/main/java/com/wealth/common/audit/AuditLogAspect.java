package com.wealth.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作审计日志切面。
 * 拦截 @AuditLog 注解方法，记录请求用户、IP、参数、耗时、结果（成功/失败）。
 * 通过独立 AUDIT_LOG logger 输出至专用审计日志文件。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT_LOG");
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** 敏感字段名（在参数日志中自动脱敏） */
    private static final String[] SENSITIVE_KEYS = {"password", "secret", "token", "idempotentKey"};

    private final JwtUtil jwtUtil;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "成功";
        String errorMsg = "";
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            status = "失败";
            errorMsg = t.getMessage() != null ? truncate(t.getMessage(), 200) : t.getClass().getSimpleName();
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            writeLog(joinPoint, auditLog, status, errorMsg, result, duration);
        }
    }

    private void writeLog(ProceedingJoinPoint jp, AuditLog ann, String status, String errorMsg, Object result, long duration) {
        try {
            HttpServletRequest request = currentRequest();
            if (request == null) return;

            String username = extractUsername(request);
            String ip = extractIp(request);
            String method = request.getMethod();
            String url = request.getRequestURI();
            String params = safeParams(jp.getArgs());

            Map<String, Object> logEntry = new LinkedHashMap<>();
            logEntry.put("@timestamp", LocalDateTime.now().format(dtf));
            logEntry.put("module", ann.module());
            logEntry.put("operation", ann.operation());
            logEntry.put("username", username);
            logEntry.put("ip", ip);
            logEntry.put("method", method);
            logEntry.put("url", url);
            logEntry.put("params", params);
            logEntry.put("status", status);
            logEntry.put("duration", duration);
            if (!errorMsg.isEmpty()) {
                logEntry.put("errorMsg", errorMsg);
            }

            auditLog.info(mapper.writeValueAsString(logEntry));
        } catch (Exception e) {
            auditLog.error("审计日志记录失败", e);
        }
    }

    /** 从请求头提取已登录用户名（JWT） */
    private String extractUsername(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                return jwtUtil.getUsernameFromToken(auth.substring(7));
            } catch (Exception ignored) {
                // token 可能已过期或无效，不影响审计记录
            }
        }
        return "anonymous";
    }

    /** 获取客户端 IP */
    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "";
    }

    /** 序列化参数时自动脱敏 */
    private String safeParams(Object[] args) {
        if (args == null || args.length == 0) return "";
        try {
            String json = mapper.writeValueAsString(args.length == 1 ? args[0] : args);
            for (String key : SENSITIVE_KEYS) {
                json = json.replaceAll("\"" + key + "\"\\s*:\\s*\"[^\"]*\"", "\"" + key + "\":\"****\"");
            }
            return truncate(json, 500);
        } catch (Exception e) {
            return Arrays.stream(args).map(a -> a != null ? a.getClass().getSimpleName() : "null")
                    .collect(Collectors.joining(","));
        }
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
