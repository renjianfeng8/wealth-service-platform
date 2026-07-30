package com.wealth.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后台管理员表。 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_admin")
public class UmsAdmin extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("nick_name")
    private String nickName;

    @TableField("status")
    private Integer status;

    @TableField("avatar")
    private String avatar;

    @TableField("login_time")
    private LocalDateTime loginTime;

}
