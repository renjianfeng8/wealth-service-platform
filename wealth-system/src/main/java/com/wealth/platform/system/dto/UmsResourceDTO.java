package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 200, message = "资源描述长度不能超过200个字符")
    @Schema(description = "资源描述")
    private String description;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    private Long categoryId;
}
