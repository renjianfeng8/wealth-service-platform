package com.wealth.common.constants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthConstantTest {

    @Test
    void extractBearerToken_should_return_null_for_blank_or_non_bearer_header() {
        assertThat(AuthConstant.extractBearerToken(null)).isNull();
        assertThat(AuthConstant.extractBearerToken("")).isNull();
        assertThat(AuthConstant.extractBearerToken("Basic abc")).isNull();
        // Bearer scheme 按现有行为区分大小写
        assertThat(AuthConstant.extractBearerToken("bearer abc")).isNull();
    }

    @Test
    void extractBearerToken_should_extract_token_after_bearer_prefix() {
        assertThat(AuthConstant.extractBearerToken("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
    }

    @Test
    void extractBearerToken_should_return_empty_string_for_bare_bearer_prefix() {
        assertThat(AuthConstant.extractBearerToken("Bearer ")).isEqualTo("");
    }

    @Test
    void extractToken_should_prefer_authorization_header_over_cookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token.from.header");
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie(AuthConstant.TOKEN_COOKIE_NAME, "token.from.cookie")
        });

        assertThat(AuthConstant.extractToken(request)).isEqualTo("token.from.header");
    }

    @Test
    void extractToken_should_fallback_to_cookie_when_header_missing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie(AuthConstant.TOKEN_COOKIE_NAME, "token.from.cookie")
        });

        assertThat(AuthConstant.extractToken(request)).isEqualTo("token.from.cookie");
    }

    @Test
    void extractToken_should_return_null_when_no_token_present() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        assertThat(AuthConstant.extractToken(request)).isNull();
    }
}
