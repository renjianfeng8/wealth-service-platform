package com.wealth.platform.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员角色关联VO")
public class UmsAdminRoleRelationVO {
    @Schema(description = "ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "角色ID")
    private Long roleId;
}
