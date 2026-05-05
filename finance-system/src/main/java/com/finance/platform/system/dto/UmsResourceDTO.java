package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "资源新增/修改 DTO")
public class UmsResourceDTO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "资源名称")
    private String name;

    @Schema(description = "资源URL")
    private String url;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;
}
