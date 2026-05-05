package com.finance.platform.trade.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易委托单实体。
 */
@Data
@TableName("fin_trade_order")
public class FinTradeOrder extends BaseEntity {

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

    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;
}
