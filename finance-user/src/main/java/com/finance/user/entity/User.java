package com.finance.platform.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 账号
    private String username;

    // 密码
    private String password;

    // 昵称
    private String nickname;

    // 手机号
    private String phone;

    // 头像
    private String avatar;

    // 状态：0禁用 1正常
    private Integer status;

    // 逻辑删除：0否 1是
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}