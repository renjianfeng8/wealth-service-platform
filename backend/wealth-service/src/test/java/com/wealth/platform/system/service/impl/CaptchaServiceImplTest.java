package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.vo.CaptchaVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    @Mock
    private RedisUtil redisUtil;

    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaServiceImpl(redisUtil);
    }

    @Test
    @DisplayName("生成验证码-返回key和图片并持久化")
    void generate_shouldReturnKeyAndImageAndPersist() {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(redisUtil).safeExecuteVoid(any(Runnable.class), anyString());

        CaptchaVO vo = captchaService.generate();

        assertNotNull(vo.getCaptchaKey());
        assertNotNull(vo.getCaptchaImage());
        verify(redisUtil).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("校验验证码-参数为空抛异常")
    void verify_shouldThrowWhenParamsEmpty() {
        assertThrows(ServiceException.class, () -> captchaService.verify(null, "1234"));
        assertThrows(ServiceException.class, () -> captchaService.verify("key", ""));
    }

    @Test
    @DisplayName("校验验证码-Redis不可用时跳过（不阻塞登录）")
    void verify_shouldSkipWhenRedisUnavailable() {
        captchaService.verify("key", "1234");
    }

    @Test
    @DisplayName("校验验证码-验证码不存在报已过期")
    void verify_shouldThrowWhenKeyNotExists() {
        when(redisUtil.safeExecute(any(), any(), anyString())).thenReturn(false);

        assertThrows(ServiceException.class, () -> captchaService.verify("key", "1234"));
    }

    @Test
    @DisplayName("校验验证码-不匹配报验证码错误并删除key")
    void verify_shouldThrowWhenCodeMismatch() {
        when(redisUtil.safeExecute(any(), any(), anyString())).thenReturn(true);
        when(redisUtil.get(anyString())).thenReturn("ABCD");

        assertThrows(ServiceException.class, () -> captchaService.verify("key", "1234"));
        verify(redisUtil).delete(anyString());
    }

    @Test
    @DisplayName("校验验证码-大小写不敏感匹配成功")
    void verify_shouldPassWhenCodeMatchesIgnoreCase() {
        when(redisUtil.safeExecute(any(), any(), anyString())).thenReturn(true);
        when(redisUtil.get(anyString())).thenReturn("abcd");

        captchaService.verify("key", "ABCD");
        verify(redisUtil).delete(anyString());
    }
}
