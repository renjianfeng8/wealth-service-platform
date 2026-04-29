package com.finance.platform.account.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自选关注表实体。
 */
@Data
@TableName("fin_user_favorite")
public class FinUserFavorite {

    /**
     * 默认构造器。
     */
    public FinUserFavorite() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

