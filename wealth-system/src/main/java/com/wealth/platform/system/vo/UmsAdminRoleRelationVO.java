package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "绠＄悊鍛樿鑹插叧鑱?VO")
public class UmsAdminRoleRelationVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "绠＄悊鍛業D")
    private Long adminId;

    @Schema(description = "瑙掕壊ID")
    private Long roleId;
}
