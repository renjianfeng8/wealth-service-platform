package com.wealth.platform.trade.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 交易委托单实体。 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_trade_order")
public class WeaTradeOrder extends BaseEntity {

    /** 数据库无 order_no 列 */
    @TableField(exist = false)
    private String orderNo;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "product_code")
    private String productCode;

    @TableField("order_type")
    private Integer tradeType;

    @TableField("order_price")
    private BigDecimal entrustPrice;

    @TableField("order_quantity")
    private Integer entrustNum;

    @TableField(value = "order_status")
    private Integer orderStatus;

    @TableField("deal_price")
    private BigDecimal dealPrice;

    @TableField("deal_quantity")
    private BigDecimal dealQuantity;

    @TableField("deal_amount")
    private BigDecimal dealAmount;
}
