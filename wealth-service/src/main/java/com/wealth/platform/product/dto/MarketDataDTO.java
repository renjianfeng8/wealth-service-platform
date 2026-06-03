package com.wealth.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "行情数据新增/修改 DTO")
public class MarketDataDTO {

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码")
    private String productCode;

    @NotNull(message = "当前价格不能为空")
    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "开盘价")
    private BigDecimal openPrice;

    @Schema(description = "收盘价")
    private BigDecimal closePrice;

    @Schema(description = "最高价")
    private BigDecimal highestPrice;

    @Schema(description = "最低价")
    private BigDecimal lowestPrice;

    @Schema(description = "涨跌幅")
    private BigDecimal riseFall;

    @Schema(description = "涨跌率")
    private BigDecimal riseFallRate;

    @Schema(description = "行情时间")
    private LocalDateTime marketTime;
}
