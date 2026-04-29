package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台用户和角色关系表实体。
 */
@Data
@TableName("ums_admin_role_relation")
public class UmsAdminRoleRelation {

    /**
     * 默认构造器。
     */
    public UmsAdminRoleRelation() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID。
     */
    @TableField(value = "admin_id")
    private Long adminId;

    /**
     * 角色ID。
     */
    @TableField(value = "role_id")
    private Long roleId;
}

