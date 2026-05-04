package com.finance.platform.trade.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinTradeOrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String productCode;
    private Integer tradeType;
    private BigDecimal entrustPrice;
    private Integer entrustNum;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}