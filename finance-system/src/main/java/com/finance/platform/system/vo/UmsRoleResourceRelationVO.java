package com.finance.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色资源关联 VO")
public class UmsRoleResourceRelationVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "资源ID")
    private Long resourceId;
}
