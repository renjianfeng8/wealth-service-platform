package com.wealth.platform.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量已读 DTO")
public class BatchReadDTO {

    @Schema(description = "消息ID列表")
    private List<Long> ids;
}
