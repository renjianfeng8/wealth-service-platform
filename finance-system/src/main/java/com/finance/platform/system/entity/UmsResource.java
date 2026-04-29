package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台资源表实体。
 */
@Data
@TableName("ums_resource")
public class UmsResource {

    /**
     * 默认构造器。
     */
    public UmsResource() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 资源名称。
     */
    @TableField(value = "name")
    private String name;

    /**
     * 资源URL。
     */
    @TableField(value = "url")
    private String url;

    /**
     * 资源描述。
     */
    @TableField(value = "description")
    private String description;

    /**
     * 资源分类ID。
     */
    @TableField(value = "category_id")
    private Long categoryId;
}

