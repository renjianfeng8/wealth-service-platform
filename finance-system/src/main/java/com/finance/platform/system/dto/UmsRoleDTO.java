package com.finance.platform.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色新增/修改 DTO")
public class UmsRoleDTO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态 0禁用 1正常")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}
