package com.wealth.platform.user.controller;

import com.wealth.common.result.Result;
import com.wealth.platform.user.dto.UserBatchDeleteDTO;
import com.wealth.platform.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Test
    @DisplayName("批量删除-传入ID列表时调用removeByIds并返回成功")
    void deleteBatch_should_call_removeByIds_and_return_success() {
        UserBatchDeleteDTO dto = new UserBatchDeleteDTO();
        dto.setIds(List.of(1L, 2L));
        when(userService.removeByIds(List.of(1L, 2L))).thenReturn(true);

        UserController controller = new UserController(userService);
        Result<Boolean> result = controller.deleteBatch(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
        verify(userService).removeByIds(List.of(1L, 2L));
    }
}
