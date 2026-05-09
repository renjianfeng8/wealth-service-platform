package com.finance.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 生成与验证工具。
 * jwt.secret / jwt.expire 须在 Nacos 共享配置中定义，无默认值以确保生产环境强制配置。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expire}")
    private long expire;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT 密钥长度不足，当前 " + keyBytes.length + " 字节，需要至少 32 字节（256位）");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Token */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 从 Token 获取用户名 */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.info("JWT验证成功");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT验证失败：Token 已过期");
        } catch (io.jsonwebtoken.security.SignatureException e) {
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
}
