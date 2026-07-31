package com.wealth.gateway.filter;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway 全局 JWT 认证过滤器。
 * 所有请求先经过此过滤器校验 Token，白名单路径直接放行。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("Gateway 认证过滤器 | 请求路径：{}", path);

        // 白名单路径直接放行（引用 AuthConstant，与 Service 层保持一致）
        for (String permitUrl : AuthConstant.PERMIT_ALL_URLS) {
            if (pathMatcher.match(permitUrl, path)) {
                return chain.filter(exchange);
            }
        }

        // 从请求头或 httpOnly Cookie 中获取 Token
        String token = AuthConstant.extractBearerToken(
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

        if (token == null) {
            // 降级：从 httpOnly Cookie 获取（防 XSS 窃取 JWT）
            HttpCookie tokenCookie = exchange.getRequest().getCookies().getFirst(AuthConstant.TOKEN_COOKIE_NAME);
            if (tokenCookie != null) {
                token = tokenCookie.getValue();
            }
        }

        if (token == null) {
            log.warn("无Token，返回401 | 路径：{}", path);
            return unauthorized(exchange, "未登录");
        }

        // 校验 Token 并提取用户身份
        try {
            Claims claims = jwtUtil.parseClaims(token);

            // 将用户身份传递到下游服务（注入请求头）
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Name", claims.getSubject())
                                   .header("X-User-Jti", claims.getId()))
                    .build();

            log.debug("Token校验通过 | 路径：{} | 用户：{}", path, claims.getSubject());
            return chain.filter(mutatedExchange);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token已过期 | 路径：{}", path);
            return unauthorized(exchange, ResultCode.TOKEN_EXPIRED.getMessage());
        } catch (Exception e) {
            log.warn("Token无效 | 路径：{}, 错误：{}", path, e.getMessage());
            return unauthorized(exchange, ResultCode.TOKEN_INVALID.getMessage());
        }
    }

    /** 返回 401 未授权响应 */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // 高优先级，在所有过滤器之前执行
    }
}
