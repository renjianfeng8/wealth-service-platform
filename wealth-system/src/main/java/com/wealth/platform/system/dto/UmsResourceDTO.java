package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "璧勬簮鏂板/淇敼 DTO")
public class UmsResourceDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "璧勬簮鍚嶇О涓嶈兘涓虹┖")
    @Schema(description = "璧勬簮鍚嶇О")
    private String name;

    @NotBlank(message = "璧勬簮URL涓嶈兘涓虹┖")
    @Schema(description = "璧勬簮URL")
    private String url;

    @Schema(description = "璧勬簮鎻忚堪")
    private String description;

    @Schema(description = "鍒嗙被ID")
    private Long categoryId;
}
