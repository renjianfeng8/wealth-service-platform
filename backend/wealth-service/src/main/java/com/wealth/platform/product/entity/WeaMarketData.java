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

    @TableField("current_price")
    private BigDecimal currentPrice;

    @TableField("open_price")
    private BigDecimal openPrice;

    @TableField("close_price")
    private BigDecimal closePrice;

    @TableField("highest_price")
    private BigDecimal highestPrice;

    @TableField("lowest_price")
    private BigDecimal lowestPrice;

    @TableField("rise_fall")
    private BigDecimal riseFall;

    @TableField("rise_fall_rate")
    private BigDecimal riseFallRate;

    @TableField("market_time")
    private LocalDateTime marketTime;

    /** 行情表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
