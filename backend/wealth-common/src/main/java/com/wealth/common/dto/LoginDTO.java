package com.wealth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码 KEY（需先调用 /captcha 获取）")
    private String captchaKey;

    @Schema(description = "验证码（4 位数字/字母）")
    private String captchaCode;
}
