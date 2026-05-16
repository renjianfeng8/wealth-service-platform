package com.wealth.platform.trade.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 浜ゆ槗濮旀墭鍗曞疄浣撱€? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_trade_order")
public class WeaTradeOrder extends BaseEntity {

    @TableField(value = "order_no")
    private String orderNo;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "product_code")
    private String productCode;

    @TableField(value = "trade_type")
    private Integer tradeType;

    @TableField(value = "entrust_price")
    private BigDecimal entrustPrice;

    @TableField(value = "entrust_num")
    private Integer entrustNum;

    @TableField(value = "order_status")
    private Integer orderStatus;
}
