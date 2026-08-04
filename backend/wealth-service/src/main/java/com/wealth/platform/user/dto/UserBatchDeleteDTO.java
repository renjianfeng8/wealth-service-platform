package com.wealth.platform.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量删除用户 DTO")
public class UserBatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    @Schema(description = "用户ID列表")
    private List<Long> ids;
}
