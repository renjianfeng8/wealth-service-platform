package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "验证码 VO")
public class CaptchaVO {
    @Schema(description = "验证码唯一标识（缓存 key 后缀）")
    private String captchaKey;

    @Schema(description = "验证码图片（Base64）")
    private String captchaImage;
}
