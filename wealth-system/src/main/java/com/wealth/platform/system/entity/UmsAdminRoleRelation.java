package com.wealth.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 鍚庡彴鐢ㄦ埛鍜岃鑹插叧绯昏〃銆? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_admin_role_relation")
public class UmsAdminRoleRelation extends BaseEntity {

    @TableField(value = "admin_id")
    private Long adminId;

    @TableField(value = "role_id")
    private Long roleId;

    /** 鍏宠仈琛ㄦ棤 create_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime createTime;

    /** 鍏宠仈琛ㄦ棤 update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
