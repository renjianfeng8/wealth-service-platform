package com.wealth.platform.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行情数据表实体 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_market_data")
public class WeaMarketData extends BaseEntity {

    @TableField("product_code")
    private String productCode;

    @TableField("price")
    private BigDecimal currentPrice;

    @TableField("open_price")
    private BigDecimal openPrice;

    @TableField("pre_close_price")
    private BigDecimal closePrice;

    @TableField("high_price")
    private BigDecimal highestPrice;

    @TableField("low_price")
    private BigDecimal lowestPrice;

    @TableField(exist = false)
    private BigDecimal riseFall;

    @TableField(exist = false)
    private BigDecimal riseFallRate;

    @TableField(exist = false)
    private LocalDateTime marketTime;

    private LocalDateTime updateTime;
}
