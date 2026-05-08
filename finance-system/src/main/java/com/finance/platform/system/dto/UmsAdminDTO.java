package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @Schema(description = "头像")
    private String avatar;
}
