package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员角色关联 DTO")
public class UmsAdminRoleRelationDTO {
    @Schema(description = "ID")
    private Long id;

    @NotNull(message = "管理员ID不能为空")
    @Schema(description = "管理员ID")
    private Long adminId;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;
}
