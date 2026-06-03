package com.wealth.platform.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "行情数据VO")
public class MarketDataVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "产品代码")
    private String productCode;

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

    @Schema(description = "涨跌额")
    private BigDecimal riseFall;

    @Schema(description = "涨跌幅")
    private BigDecimal riseFallRate;

    @Schema(description = "行情时间")
    private LocalDateTime marketTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
