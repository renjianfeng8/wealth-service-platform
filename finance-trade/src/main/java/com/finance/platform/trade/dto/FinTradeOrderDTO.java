package com.finance.platform.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "交易委托 DTO")
public class FinTradeOrderDTO {

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
    @Schema(description = "委托价格")
    private BigDecimal entrustPrice;

    @NotNull(message = "委托数量不能为空")
    @Schema(description = "委托数量")
    private Integer entrustNum;
}
