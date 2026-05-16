package com.wealth.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 鍚庡彴瑙掕壊璧勬簮鍏崇郴琛ㄣ€? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_role_resource_relation")
public class UmsRoleResourceRelation extends BaseEntity {

    @TableField(value = "role_id")
    private Long roleId;

    @TableField(value = "resource_id")
    private Long resourceId;

    /** 鍏宠仈琛ㄦ棤 create_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime createTime;

    /** 鍏宠仈琛ㄦ棤 update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
