package com.finance.platform.trade.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinTradeOrderDTO {
    private Long userId;
    private String productCode;
    private Integer tradeType;
    private BigDecimal entrustPrice;
    private Integer entrustNum;
}