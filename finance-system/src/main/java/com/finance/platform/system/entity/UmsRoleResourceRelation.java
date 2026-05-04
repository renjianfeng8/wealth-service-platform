package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
@TableName("ums_role_resource_relation")
public class UmsRoleResourceRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "role_id")
    private Long roleId;

    @TableField(value = "resource_id")
    private Long resourceId;

    // 统一加逻辑删除
    @TableLogic
    private Integer delFlag;
}