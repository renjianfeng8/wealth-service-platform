package com.wealth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "站内消息 Feign DTO")
public class MessageFeignDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "消息类型 1系统 2交易 3风控")
    private Integer msgType;

    @Schema(description = "消息标题")
    private String msgTitle;

    @Schema(description = "消息内容")
    private String msgContent;
}
