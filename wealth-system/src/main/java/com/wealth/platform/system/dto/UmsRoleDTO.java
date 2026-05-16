package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "瑙掕壊鏂板/淇敼 DTO")
public class UmsRoleDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "瑙掕壊鍚嶇О涓嶈兘涓虹┖")
    @Schema(description = "瑙掕壊鍚嶇О")
    private String name;

    @Schema(description = "瑙掕壊鎻忚堪")
    private String description;

    @Schema(description = "鐘舵€?0绂佺敤 1姝ｅ父")
    private Integer status;

    @Schema(description = "鎺掑簭")
    private Integer sort;
}
