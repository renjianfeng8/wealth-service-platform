package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后台用户和角色关系表。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_admin_role_relation")
public class UmsAdminRoleRelation extends BaseEntity {

    @TableField(value = "admin_id")
    private Long adminId;

    @TableField(value = "role_id")
    private Long roleId;

    /** 关联表无 create_time 列 */
    @TableField(exist = false)
    private LocalDateTime createTime;

    /** 关联表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** 关联表无 del_flag 列 */
    @TableField(exist = false)
    private Integer delFlag;
}
