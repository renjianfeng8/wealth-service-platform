package com.wealth.platform.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "资讯新增/修改 DTO")
public class FinNewsDTO {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "内容")
    private String content;

    @Schema(description = "资讯类型")
    private Integer newsType;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
}
