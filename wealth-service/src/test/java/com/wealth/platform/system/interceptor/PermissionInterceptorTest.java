package com.wealth.platform.system.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.UmsAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UmsAdminService adminService;

    @Mock
    private PermissionCacheService permissionCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new PermissionInterceptor(jwtUtil, adminService, permissionCacheService, objectMapper);
    }

    @Test
    void preHandle_should_write_unauthorized_response_when_token_missing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).isEqualTo("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
    }
}
