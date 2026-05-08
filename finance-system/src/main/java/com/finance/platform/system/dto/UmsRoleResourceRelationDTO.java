package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "角色资源关联 DTO")
public class UmsRoleResourceRelationDTO {
    @Schema(description = "ID")
    private Long id;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;

    @NotNull(message = "资源ID不能为空")
    @Schema(description = "资源ID")
    private Long resourceId;
}
