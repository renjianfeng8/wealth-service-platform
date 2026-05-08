package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "资源新增/修改 DTO")
public class UmsResourceDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "资源名称不能为空")
    @Schema(description = "资源名称")
    private String name;

    @NotBlank(message = "资源URL不能为空")
    @Schema(description = "资源URL")
    private String url;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;
}
