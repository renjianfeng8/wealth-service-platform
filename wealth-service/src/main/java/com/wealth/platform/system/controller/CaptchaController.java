package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.common.utils.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/system")
@Tag(name = "验证码管理")
public class CaptchaController {

    private static final long CAPTCHA_TTL_MINUTES = 5;
    private static final String KEY_CAPTCHA = "captcha:";

    private final RedisUtil redisUtil;

    public CaptchaController(ObjectProvider<RedisUtil> redisUtilProvider) {
        this.redisUtil = redisUtilProvider.getIfAvailable();
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码（图片 Base64）")
    public Result<Map<String, String>> getCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text();
        String key = UUID.randomUUID().toString().replace("-", "");

        if (redisUtil != null) {
            try {
                redisUtil.set(KEY_CAPTCHA + key, code, CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (DataAccessException e) {
                log.warn("Redis 不可用，验证码未持久化: {}", e.getMessage());
            }
        } else {
            log.warn("RedisUtil 不可用，验证码未持久化");
        }

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", captcha.toBase64());
        return Result.success(result);
    }
}
