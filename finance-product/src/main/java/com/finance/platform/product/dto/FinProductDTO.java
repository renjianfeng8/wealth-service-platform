package com.finance.platform.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinProductDTO {
    private String productName;
    private String productCode;
    private Integer productType;
    private BigDecimal price;
    private BigDecimal riseFall;
    private BigDecimal riseFallRate;
    private Integer status;
    private Integer sort;
}