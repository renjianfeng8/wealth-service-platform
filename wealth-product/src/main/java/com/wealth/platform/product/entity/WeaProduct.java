package com.wealth.platform.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 理财/贵金属产品表实体 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_product")
public class WeaProduct extends BaseEntity {

    @TableField("product_name")
    private String productName;

    @TableField("product_code")
    private String productCode;

    @TableField("product_type")
    private Integer productType;

    @TableField("price")
    private BigDecimal price;

    @TableField("rise_fall")
    private BigDecimal riseFall;

    @TableField("rise_fall_rate")
    private BigDecimal riseFallRate;

    @TableField("status")
    private Integer status;

    @TableField("sort")
    private Integer sort;
}
