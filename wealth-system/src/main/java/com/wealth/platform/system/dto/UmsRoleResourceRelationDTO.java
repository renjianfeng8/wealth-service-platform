package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "瑙掕壊璧勬簮鍏宠仈 DTO")
public class UmsRoleResourceRelationDTO {
    @Schema(description = "ID")
    private Long id;

    @NotNull(message = "瑙掕壊ID涓嶈兘涓虹┖")
    @Schema(description = "瑙掕壊ID")
    private Long roleId;

    @NotNull(message = "璧勬簮ID涓嶈兘涓虹┖")
    @Schema(description = "璧勬簮ID")
    private Long resourceId;
}
