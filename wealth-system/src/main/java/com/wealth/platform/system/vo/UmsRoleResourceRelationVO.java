package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "瑙掕壊璧勬簮鍏宠仈 VO")
public class UmsRoleResourceRelationVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "瑙掕壊ID")
    private Long roleId;

    @Schema(description = "璧勬簮ID")
    private Long resourceId;
}
