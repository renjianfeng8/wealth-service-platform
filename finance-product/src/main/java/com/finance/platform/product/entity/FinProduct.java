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
 * 理财/贵金属产品表实体。
 */
@Data
@TableName("fin_product")
public class FinProduct {

    /**
     * 默认构造器。
     */
    public FinProduct() {
    }

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称。
     */
    @TableField("product_name")
    private String productName;

    /**
     * 产品编码。
     */
    @TableField("product_code")
    private String productCode;

    /**
     * 类型：1黄金 2白银 3理财。
     */
    @TableField("product_type")
    private Integer productType;

    /**
     * 当前单价。
     */
    @TableField("price")
    private BigDecimal price;

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
     * 上下架：0下架 1上架。
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序。
     */
    @TableField("sort")
    private Integer sort;

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

    /**
     * 更新时间。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

