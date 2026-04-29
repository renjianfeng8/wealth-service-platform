package com.finance.platform.trade.entity;

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
 * 交易委托单实体。
 */
@Data
@TableName("fin_trade_order")
public class FinTradeOrder {

    /**
     * 默认构造器。
     */
    public FinTradeOrder() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 委托单号。
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 用户ID。
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 产品编码。
     */
    @TableField(value = "product_code")
    private String productCode;

    /**
     * 交易类型：1买入 2卖出。
     */
    @TableField(value = "trade_type")
    private Integer tradeType;

    /**
     * 委托价格。
     */
    @TableField(value = "entrust_price")
    private BigDecimal entrustPrice;

    /**
     * 委托数量。
     */
    @TableField(value = "entrust_num")
    private Integer entrustNum;

    /**
     * 状态：1待委托 2已完成 3已撤销。
     */
    @TableField(value = "order_status")
    private Integer orderStatus;

    /**
     * 逻辑删除标识：0未删除 1已删除。
     */
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

