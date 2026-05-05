package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台角色表。
 */
@Data
@TableName("ums_role")
public class UmsRole extends BaseEntity {

    private String name;

    private String description;

    private Integer status;

    private Integer sort;

    /** ums_role 表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /** ums_role 表无 del_flag 列 */
    @TableField(exist = false)
    private Integer delFlag;
}
