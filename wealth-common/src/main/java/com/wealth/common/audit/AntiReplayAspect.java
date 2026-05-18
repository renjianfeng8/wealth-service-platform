package com.wealth.common.audit;

import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防重放切面。
 * 拦截 @AntiReplay 注解方法，校验：
 * 1. X-Timestamp 是否在时间窗口内（防时间篡改）
 * 2. X-Nonce 在 Redis 中是否已存在（防重复提交）
 * 不传 X-Timestamp/X-Nonce 头的旧客户端仍可正常访问（兼容降级）。
 */
@Aspect
@Component
public class AntiReplayAspect {

    private static final Logger log = LoggerFactory.getLogger(AntiReplayAspect.class);
    private static final String NONCE_KEY_PREFIX = "nonce:";

    private final RedisUtil redisUtil;

    public AntiReplayAspect(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Around("@annotation(antiReplay)")
    public Object around(ProceedingJoinPoint joinPoint, AntiReplay antiReplay) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String timestampStr = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");

        // 兼容旧客户端：未传防重包头时直接放行
        if (timestampStr == null || nonce == null || nonce.isBlank()) {
            return joinPoint.proceed();
        }

        int timeWindow = antiReplay.timeWindow();

        // 1. 校验时间戳
        long now = System.currentTimeMillis();
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new ServiceException(400, "X-Timestamp 格式错误");
        }

        long diff = Math.abs(now - timestamp);
        if (diff > timeWindow * 1000L) {
            log.warn("防重放校验失败：时间戳超出窗口 | diff={}ms | window={}s", diff, timeWindow);
            throw new ServiceException(400, "请求已过期，请重新发送");
        }

        // 2. 校验 nonce 唯一性（SET NX，TTL 等于时间窗口）
        String redisKey = NONCE_KEY_PREFIX + nonce;
        Boolean success = redisUtil.setIfAbsent(redisKey, "1", timeWindow, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            log.warn("防重放校验失败：nonce 重复使用 | nonce={}", nonce);
            throw new ServiceException(400, "请勿重复提交");
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
