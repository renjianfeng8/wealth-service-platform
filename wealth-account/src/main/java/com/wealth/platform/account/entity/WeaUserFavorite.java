package com.wealth.platform.account.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛鑷€夊叧娉ㄨ〃瀹炰綋銆? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_user_favorite")
public class WeaUserFavorite extends BaseEntity {

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "product_code")
    private String productCode;

    /** 鑷€夎〃鏃?update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** 鑷€夎〃鏃?del_flag 鍒?*/
    @TableField(exist = false)
    private Integer delFlag;
}
