package com.wealth.platform.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量已读 DTO")
public class BatchReadDTO {

    @NotEmpty(message = "消息ID列表不能为空")
    @Schema(description = "消息ID列表")
    private List<Long> ids;
}
