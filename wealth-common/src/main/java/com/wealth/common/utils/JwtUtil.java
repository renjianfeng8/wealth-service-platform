package com.wealth.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 生成与验证工具。
 * jwt.secret / jwt.expire 需在 Nacos 共享配置中定义。
 * 支持双 Token 机制：access_token（短时效）+ refresh_token（长时效）。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    /** access_token 有效期（默认 30 分钟） */
    @Value("${jwt.access-expire:1800000}")
    private long accessExpire;

    /** refresh_token 有效期（默认 7 天） */
    @Value("${jwt.refresh-expire:604800000}")
    private long refreshExpire;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT 密钥长度不足，当前" + keyBytes.length + " 字节，需要至少 32 字节（256位）");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Token（向后兼容），有效期由 jwt.access-expire 决定 */
    public String generateToken(String username) {
        return generateToken(username, accessExpire);
    }

    /** 生成 access_token（短时效） */
    public String generateAccessToken(String username) {
        return generateToken(username, accessExpire);
    }

    /** 生成 refresh_token（长时效） */
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshExpire);
    }

    /** 生成 token 对 */
    public TokenPair generateTokenPair(String username) {
        String accessToken = generateAccessToken(username);
        String refreshToken = generateRefreshToken(username);
        return new TokenPair(accessToken, refreshToken, accessExpire);
    }

    /** 从 Token 获取用户名 */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /** 从 Token 获取 jti */
    public String getTokenIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getId();
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT验证失败：Token 已过期");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("JWT验证失败：签名错误（密钥不匹配）");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT验证失败：Token 格式错误/被篡改");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("JWT验证失败：不支持的Token算法");
        } catch (Exception e) {
            log.error("JWT验证失败：{}", e.getMessage());
        }
        return false;
    }

    private String generateToken(String username, long expireMs) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Token 对响应体 */
    public record TokenPair(String accessToken, String refreshToken, long expiresIn) {}
}
