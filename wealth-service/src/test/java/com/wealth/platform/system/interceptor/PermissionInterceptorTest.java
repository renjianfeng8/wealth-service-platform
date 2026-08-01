package com.wealth.platform.system.interceptor;

import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.service.UmsAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UmsAdminService adminService;

    private PermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new PermissionInterceptor(jwtUtil, adminService);
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

    @Test
    void preHandle_should_write_forbidden_when_no_permission() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/admin");
        request.addHeader("Authorization", "Bearer valid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.validateToken("valid.token")).thenReturn(true);
        when(adminService.checkPermissionForToken("valid.token", "/system/admin")).thenReturn(false);

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).isEqualTo("{\"code\":403,\"message\":\"无权限访问\",\"data\":null}");
    }

    @Test
    void preHandle_should_allow_when_token_valid_and_permission_granted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/admin");
        request.addHeader("Authorization", "Bearer valid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.validateToken("valid.token")).thenReturn(true);
        when(adminService.checkPermissionForToken("valid.token", "/system/admin")).thenReturn(true);

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
