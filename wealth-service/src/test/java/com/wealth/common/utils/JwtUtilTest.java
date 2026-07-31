package com.wealth.common.utils;

import com.wealth.common.utils.JwtUtil.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    /** 与 application.yml 默认密钥一致（长度 ≥32 字节） */
    private static final String SECRET = "wealth-micro-service-20260501-very-safe-secret-key-123456789";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpire", 1800000L);   // 30 分钟
        ReflectionTestUtils.setField(jwtUtil, "refreshExpire", 604800000L); // 7 天
        jwtUtil.init();
    }

    @Test
    void generateTokenPair_should_produce_parsable_tokens() {
        TokenPair pair = jwtUtil.generateTokenPair("admin");

        Claims accessClaims = jwtUtil.parseClaims(pair.accessToken());
        assertThat(accessClaims.getSubject()).isEqualTo("admin");
        assertThat(accessClaims.getId()).isNotBlank();
        assertThat(accessClaims.getExpiration()).isNotNull();

        Claims refreshClaims = jwtUtil.parseClaims(pair.refreshToken());
        assertThat(refreshClaims.getSubject()).isEqualTo("admin");
        assertThat(refreshClaims.getId()).isNotBlank();
    }

    @Test
    void getUsernameFromToken_should_return_subject_and_userType() {
        String token = jwtUtil.generateToken("alice", "user");

        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(jwtUtil.getUserTypeFromToken(token)).isEqualTo("user");
    }

    @Test
    void getTokenIdFromToken_should_return_jti() {
        TokenPair pair = jwtUtil.generateTokenPair("admin");

        String jti = jwtUtil.getTokenIdFromToken(pair.refreshToken());
        assertThat(jti).isEqualTo(jwtUtil.parseClaims(pair.refreshToken()).getId());
    }

    @Test
    void validateToken_should_return_true_for_valid_token() {
        String token = jwtUtil.generateToken("admin");

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_should_return_false_for_tampered_token() {
        String token = jwtUtil.generateToken("admin");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_should_return_false_for_garbage_token() {
        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void parseClaims_should_throw_expired_when_token_expired() {
        ReflectionTestUtils.setField(jwtUtil, "accessExpire", -1000L);
        String expiredToken = jwtUtil.generateToken("admin");

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
        assertThatThrownBy(() -> jwtUtil.parseClaims(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
