package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员表实体。
 */
@Data
@TableName("ums_admin")
public class UmsAdmin {

    /**
     * 默认构造器。
     */
    public UmsAdmin() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名。
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码。
     */
    @TableField(value = "password")
    private String password;

    /**
     * 邮箱。
     */
    @TableField(value = "email")
    private String email;

    /**
     * 昵称。
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 启用状态：0禁用；1启用。
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 登录时间。
     */
    @TableField(value = "login_time")
    private LocalDateTime loginTime;

    /**
     * 头像。
     */
    @TableField(value = "avatar")
    private String avatar;
}

