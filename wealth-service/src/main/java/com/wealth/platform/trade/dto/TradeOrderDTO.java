package com.wealth.platform.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "交易委托 DTO")
public class TradeOrderDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码")
    private String productCode;

    @NotNull(message = "交易类型不能为空")
    @Schema(description = "交易类型 1买入 2卖出")
    private Integer tradeType;

    @NotNull(message = "委托价格不能为空")
    @DecimalMin(value = "0.01", message = "委托价格必须大于0")
    @Schema(description = "委托价格")
    private java.math.BigDecimal entrustPrice;

    @NotNull(message = "委托数量不能为空")
    @Min(value = 1, message = "委托数量不能小于1")
    @Schema(description = "委托数量")
    private Integer entrustNum;

    @Schema(description = "幂等键（客户端生成UUID传入，防重复提交）")
    private String idempotentKey;
}
