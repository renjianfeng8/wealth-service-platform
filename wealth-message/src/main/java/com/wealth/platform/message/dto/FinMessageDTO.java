package com.wealth.platform.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "站内消息 DTO")
public class FinMessageDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "消息类型 1系统 2交易 3风控")
    private Integer msgType;

    @NotBlank(message = "消息标题不能为空")
    @Schema(description = "消息标题")
    private String msgTitle;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容")
    private String msgContent;
}
