package com.finance.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统用户表实体。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status;
}
