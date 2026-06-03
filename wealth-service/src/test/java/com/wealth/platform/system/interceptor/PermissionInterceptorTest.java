package com.wealth.platform.system.interceptor;

import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
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

    @Mock
    private UmsAdminRoleRelationService adminRoleRelationService;

    @Mock
    private UmsRoleResourceRelationService roleResourceRelationService;

    @Mock
    private ObjectProvider<RedisUtil> redisUtilProvider;

    private PermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        when(redisUtilProvider.getIfAvailable()).thenReturn(null);
        interceptor = new PermissionInterceptor(jwtUtil, adminService, adminRoleRelationService,
                roleResourceRelationService, redisUtilProvider);
    }

    @Test
    void preHandle_should_write_unauthorized_response_when_token_missing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).isEqualTo("{\"code\":401,\"message\":\"未登录\"}");
    }
}
