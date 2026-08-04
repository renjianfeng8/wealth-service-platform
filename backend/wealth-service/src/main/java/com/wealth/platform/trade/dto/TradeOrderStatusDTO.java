package com.wealth.platform.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "交易委托状态更新 DTO")
public class TradeOrderStatusDTO {

    @NotNull(message = "订单状态不能为空")
    @Min(value = 1, message = "订单状态不能小于1")
    @Max(value = 3, message = "订单状态不能大于3")
    @Schema(description = "订单状态 1已提交 2已成交 3已撤销")
    private Integer orderStatus;
}
