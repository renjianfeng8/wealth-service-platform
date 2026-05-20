package com.wealth.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Gateway 全局 JWT 认证过滤器。
 * 所有请求先经过此过滤器校验 Token，白名单路径直接放行。
 */
@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 无需认证的白名单路径（Swagger/Knife4j 路径已移除，需登录后访问） */
    private static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/login",
            "/system/captcha",
            "/user/login",
            "/user/register",
            "/product/WeaMarketData/sse/**",
    };

    /** JWT Cookie 名称（httpOnly，防 XSS 窃取） */
    private static final String TOKEN_COOKIE_NAME = "wealth_token";

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.error("JWT 密钥长度不足，当前 {} 字节，需要至少 32 字节", keyBytes.length);
            throw new IllegalStateException(
                    "JWT 密钥长度不足，当前" + keyBytes.length + " 字节，需要至少 32 字节（256位）");
        }
        log.info("Gateway JWT 认证过滤器初始化完成，密钥长度：{} 字节", keyBytes.length);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("Gateway 认证过滤器 | 请求路径：{}", path);

        // 白名单路径直接放行
        for (String permitUrl : PERMIT_ALL_URLS) {
            if (pathMatcher.match(permitUrl, path)) {
                return chain.filter(exchange);
            }
        }

        // 从请求头或 httpOnly Cookie 中获取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // 降级：从 httpOnly Cookie 获取（防 XSS 窃取 JWT）
            HttpCookie tokenCookie = exchange.getRequest().getCookies().getFirst(TOKEN_COOKIE_NAME);
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
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 将用户身份传递到下游服务（注入请求头）
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Name", claims.getSubject())
                                   .header("X-User-Jti", claims.getId()))
                    .build();

            log.debug("Token校验通过 | 路径：{} | 用户：{}", path, claims.getSubject());
            return chain.filter(mutatedExchange);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token已过期 | 路径：{}", path);
            return unauthorized(exchange, "Token已过期");
        } catch (Exception e) {
            log.warn("Token无效 | 路径：{}, 错误：{}", path, e.getMessage());
            return unauthorized(exchange, "Token无效");
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
