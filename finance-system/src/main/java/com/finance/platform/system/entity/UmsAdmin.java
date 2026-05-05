package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员表。
 */
@Data
@TableName("ums_admin")
public class UmsAdmin extends BaseEntity {

    private String username;

    private String password;

    private String email;

    private String nickName;

    private Integer status;

    private String avatar;

    private LocalDateTime loginTime;

    /** ums_admin 表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** ums_admin 表无 del_flag 列 */
    @TableField(exist = false)
    private Integer delFlag;
}
