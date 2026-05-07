package com.finance.platform.account.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户自选关注表实体。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("fin_user_favorite")
public class FinUserFavorite extends BaseEntity {

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "product_code")
    private String productCode;

    /** 自选表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** 自选表无 del_flag 列 */
    @TableField(exist = false)
    private Integer delFlag;
}
