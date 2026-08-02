package com.wealth.platform.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {
    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户类型 admin/user")
    private String userType;

    @Schema(description = "Token 有效期（秒）")
    private long expiresInSeconds;

    @Schema(description = "refresh_token（用于静默续期与登出黑名单）")
    private String refreshToken;
}
