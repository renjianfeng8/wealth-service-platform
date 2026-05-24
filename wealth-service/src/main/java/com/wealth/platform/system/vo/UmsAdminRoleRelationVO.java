package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员角色关联VO")
public class UmsAdminRoleRelationVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "角色ID")
    private Long roleId;
}
