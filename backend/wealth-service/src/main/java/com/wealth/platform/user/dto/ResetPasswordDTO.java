package com.wealth.platform.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "重置密码 DTO")
public class ResetPasswordDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码（用于身份验证）")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String password;
}
