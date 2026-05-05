package com.finance.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.Data;

/**
 * 系统用户表实体。
 */
@Data
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private Integer status;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
