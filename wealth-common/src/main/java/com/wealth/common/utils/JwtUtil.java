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
 * 支持双 Token 机制：access_token（短时效）+ refresh_token（长时效）。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey cachedSigningKey;

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
        this.cachedSigningKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        return cachedSigningKey;
    }

    /** 生成 Token（向后兼容），有效期由 jwt.access-expire 决定 */
    public String generateToken(String username) {
        return generateToken(username, accessExpire);
    }

    /** 生成带 userType 的 Token */
    public String generateToken(String username, String userType) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userType", userType)
                .expiration(new Date(System.currentTimeMillis() + accessExpire))
                .signWith(getSigningKey())
                .compact();
    }

    /** 从 Token 获取 userType */
    public String getUserTypeFromToken(String token) {
        return parseClaims(token).get("userType", String.class);
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
        return parseClaims(token).getSubject();
    }

    /** 从 Token 获取 jti */
    public String getTokenIdFromToken(String token) {
        return parseClaims(token).getId();
    }

    /**
     * 解析 Token 并返回 Claims（签名校验通过后）。
     * 收敛各解析方法的重复 parser 构建；暴露给 Gateway 过滤器以区分 Token 过期与其他无效场景。
     *
     * @throws ExpiredJwtException Token 已过期
     * @throws io.jsonwebtoken.JwtException Token 无效（签名错误 / 格式错误 / 算法不支持等）
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
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
