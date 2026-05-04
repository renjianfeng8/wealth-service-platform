package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
@TableName("ums_admin_role_relation")
public class UmsAdminRoleRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "admin_id")
    private Long adminId;

    @TableField(value = "role_id")
    private Long roleId;

    // 统一加逻辑删除
    @TableLogic
    private Integer delFlag;
}