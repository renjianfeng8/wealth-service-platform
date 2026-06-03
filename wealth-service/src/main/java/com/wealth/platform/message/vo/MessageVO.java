package com.wealth.platform.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "消息VO")
public class MessageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "消息类型")
    private Integer msgType;

    @Schema(description = "消息标题")
    private String msgTitle;

    @Schema(description = "消息内容")
    private String msgContent;

    @Schema(description = "已读标志")
    private Integer readFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
