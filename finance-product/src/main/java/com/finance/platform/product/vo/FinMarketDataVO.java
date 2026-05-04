package com.finance.platform.product.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinMarketDataVO {
    private Long id;
    private String productCode;
    private BigDecimal currentPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highestPrice;
    private BigDecimal lowestPrice;
    private BigDecimal riseFall;
    private BigDecimal riseFallRate;
    private LocalDateTime marketTime;
    private LocalDateTime createTime;
}