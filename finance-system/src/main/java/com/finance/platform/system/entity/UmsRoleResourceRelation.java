package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台角色资源关系表实体。
 */
@Data
@TableName("ums_role_resource_relation")
public class UmsRoleResourceRelation {

    /**
     * 默认构造器。
     */
    public UmsRoleResourceRelation() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID。
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 资源ID。
     */
    @TableField(value = "resource_id")
    private Long resourceId;
}

