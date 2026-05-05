package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员角色关联 DTO")
public class UmsAdminRoleRelationDTO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "角色ID")
    private Long roleId;
}
