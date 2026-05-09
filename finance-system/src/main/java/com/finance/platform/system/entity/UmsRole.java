package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后台角色表。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_role")
public class UmsRole extends BaseEntity {

    private String name;

    private String description;

    private Integer status;

    private Integer sort;

    /** ums_role 表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** ums_role 表有 del_flag 列，使用逻辑删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
