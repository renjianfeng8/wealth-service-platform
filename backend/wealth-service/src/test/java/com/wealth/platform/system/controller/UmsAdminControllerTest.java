package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.platform.system.service.UmsAdminAuthService;
import com.wealth.platform.system.service.UmsAdminCrudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminControllerTest {

    @Mock
    private UmsAdminAuthService umsAdminAuthService;
    @Mock
    private UmsAdminCrudService umsAdminCrudService;

    @Test
    @DisplayName("刷新token-返回新token对并写入access cookie")
    void refresh_should_return_new_token_pair_and_set_cookie() {
        TokenPair pair = new TokenPair("new.access.token", "new.refresh.token", 1800000);
        when(umsAdminAuthService.refreshToken("Bearer old.refresh")).thenReturn(pair);

        UmsAdminController controller = new UmsAdminController(umsAdminAuthService, umsAdminCrudService);
        ResponseEntity<Result<TokenPair>> response = controller.refresh("Bearer old.refresh");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("new.access.token", response.getBody().getData().accessToken());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("wealth_token=new.access.token"));
    }
}
