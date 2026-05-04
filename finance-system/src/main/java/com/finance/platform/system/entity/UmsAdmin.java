package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员表
 */
@Data
@TableName("ums_admin")
public class UmsAdmin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String nickName;

    private Integer status;

    private String avatar;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime loginTime;

    // 逻辑删除（和 sys_user 保持统一！）
    @TableLogic
    private Integer delFlag;
}