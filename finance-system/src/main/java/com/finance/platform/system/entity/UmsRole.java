package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台角色表实体。
 */
@Data
@TableName("ums_role")
public class UmsRole {

    /**
     * 默认构造器。
     */
    public UmsRole() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称。
     */
    @TableField(value = "name")
    private String name;

    /**
     * 角色描述。
     */
    @TableField(value = "description")
    private String description;

    /**
     * 启用状态：0禁用；1启用。
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序。
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

