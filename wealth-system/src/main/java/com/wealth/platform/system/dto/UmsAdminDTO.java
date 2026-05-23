package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员新增/修改 DTO")
public class UmsAdminDTO {

    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱长度不能超过64个字符")
    @Schema(description = "邮箱")
    private String email;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称")
    private String nickName;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 1, message = "状态值不能大于1")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    @Schema(description = "头像")
    private String avatar;
}
