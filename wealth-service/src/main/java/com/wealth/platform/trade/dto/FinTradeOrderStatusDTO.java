package com.wealth.platform.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "交易委托状态更新 DTO")
public class FinTradeOrderStatusDTO {

    @NotNull(message = "订单状态不能为空")
    @Schema(description = "订单状态 1已提交 2已成交 3已撤销")
    private Integer orderStatus;
}
