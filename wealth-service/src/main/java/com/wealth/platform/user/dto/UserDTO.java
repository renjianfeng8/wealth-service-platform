package com.wealth.platform.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户新增/修改DTO")
public class UserDTO {

    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "账号")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    @Schema(description = "头像")
    private String avatar;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 1, message = "状态值不能大于1")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @Schema(description = "旧密码（重置密码时用于身份验证）")
    private String oldPassword;
}
