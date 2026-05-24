package com.wealth.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "角色新增/修改 DTO")
public class UmsRoleDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
    private String name;

    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    @Schema(description = "角色描述")
    private String description;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 1, message = "状态值不能大于1")
    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 9999, message = "排序值不能大于9999")
    @Schema(description = "排序")
    private Integer sort;
}
