package com.wealth.platform.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "资源 VO")
public class UmsResourceVO {
    @Schema(description = "ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "资源名称")
    private String name;

    @Schema(description = "资源URL")
    private String url;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
