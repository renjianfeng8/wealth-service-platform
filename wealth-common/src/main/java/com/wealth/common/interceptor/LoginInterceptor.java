package com.wealth.common.interceptor;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.HttpResponseUtil;
import com.wealth.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("进入拦截器 | 请求地址：{}", uri);

        // 使用 PathMatcher 匹配放行路径（支持 Ant 风格通配符）
        for (String permitUrl : AuthConstant.PERMIT_ALL_URLS) {
            if (PATH_MATCHER.match(permitUrl, uri)) {
                return true;
            }
        }

        String token = AuthConstant.extractToken(request);
        if (token == null) {
            log.warn("无Token，返回401");
            HttpResponseUtil.writeJson(response, 401, 401, "未登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token无效，返回401");
            HttpResponseUtil.writeJson(response, 401, ResultCode.TOKEN_INVALID.getCode(), ResultCode.TOKEN_INVALID.getMessage());
            return false;
        }

        log.info("Token校验通过，放行！");
        return true;
    }

}
