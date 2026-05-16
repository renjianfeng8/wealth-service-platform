package com.wealth.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSyncDTO {
    private Long id;
    private String productName;
    private String productCode;
    private Integer productType;
    private BigDecimal price;
    private BigDecimal riseFall;
    private BigDecimal riseFallRate;
    private Integer status;
    private Integer sort;
    private Integer delFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
