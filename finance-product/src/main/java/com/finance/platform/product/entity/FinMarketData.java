package com.finance.platform.product.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行情数据表实体。
 */
@Data
@TableName("fin_market_data")
public class FinMarketData {

    /**
     * 默认构造器。
     */
    public FinMarketData() {
    }

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品编码。
     */
    @TableField("product_code")
    private String productCode;

    /**
     * 实时价格。
     */
    @TableField("current_price")
    private BigDecimal currentPrice;

    /**
     * 开盘价。
     */
    @TableField("open_price")
    private BigDecimal openPrice;

    /**
     * 昨收价。
     */
    @TableField("close_price")
    private BigDecimal closePrice;

    /**
     * 最高价。
     */
    @TableField("highest_price")
    private BigDecimal highestPrice;

    /**
     * 最低价。
     */
    @TableField("lowest_price")
    private BigDecimal lowestPrice;

    /**
     * 涨跌额。
     */
    @TableField("rise_fall")
    private BigDecimal riseFall;

    /**
     * 涨跌幅。
     */
    @TableField("rise_fall_rate")
    private BigDecimal riseFallRate;

    /**
     * 行情时间。
     */
    @TableField("market_time")
    private LocalDateTime marketTime;

    /**
     * 逻辑删除标识：0否 1是。
     */
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

