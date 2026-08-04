package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.platform.system.service.CaptchaService;
import com.wealth.platform.system.vo.CaptchaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@Tag(name = "验证码管理")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码（图片 Base64）")
    public Result<CaptchaVO> getCaptcha() {
        return Result.success(captchaService.generate());
    }
}
