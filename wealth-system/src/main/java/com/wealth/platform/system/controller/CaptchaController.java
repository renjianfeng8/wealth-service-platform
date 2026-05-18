package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.common.utils.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Tag(name = "验证码管理")
public class CaptchaController {

    private static final long CAPTCHA_TTL_MINUTES = 5;
    private static final String KEY_CAPTCHA = "captcha:";

    private final RedisUtil redisUtil;

    public CaptchaController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码（图片 Base64）")
    public Result<Map<String, String>> getCaptcha() {
        // 生成 4 位数字/字母验证码，宽 130，高 48
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text();
        String key = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，5 分钟有效
        redisUtil.set(KEY_CAPTCHA + key, code, CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", captcha.toBase64());
        return Result.success(result);
    }
}
