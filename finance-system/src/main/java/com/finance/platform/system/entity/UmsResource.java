package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后台资源表。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_resource")
public class UmsResource extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("url")
    private String url;

    @TableField("description")
    private String description;

    @TableField("category_id")
    private Long categoryId;

    /** ums_resource 表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
