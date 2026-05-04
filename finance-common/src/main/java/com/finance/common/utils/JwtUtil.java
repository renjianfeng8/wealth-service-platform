package com.finance.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expire}")
    private long expire;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 从Token获取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 验证Token是否有效（带完整日志）
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("✅ JWT验证成功");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("❌ JWT验证失败：Token 已过期");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("❌ JWT验证失败：签名错误（密钥不匹配）");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("❌ JWT验证失败：Token 格式错误/被篡改");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            System.out.println("❌ JWT验证失败：不支持的Token算法");
        } catch (Exception e) {
            System.out.println("❌ JWT验证失败：" + e.getMessage());
        }
        return false;
    }
}