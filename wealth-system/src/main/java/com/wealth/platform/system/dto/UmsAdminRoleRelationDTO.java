package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "绠＄悊鍛樿鑹插叧鑱?DTO")
public class UmsAdminRoleRelationDTO {
    @Schema(description = "ID")
    private Long id;

    @NotNull(message = "绠＄悊鍛業D涓嶈兘涓虹┖")
    @Schema(description = "绠＄悊鍛業D")
    private Long adminId;

    @NotNull(message = "瑙掕壊ID涓嶈兘涓虹┖")
    @Schema(description = "瑙掕壊ID")
    private Long roleId;
}
