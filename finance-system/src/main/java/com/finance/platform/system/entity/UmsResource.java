package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ums_resource")
public class UmsResource {

    // 自增 ID（和你项目一致）
    @TableId(type = IdType.AUTO)
    private Long id;

    // 创建时间（插入时自动填充）
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 资源名称
    @TableField("name")
    private String name;

    // 资源URL
    @TableField("url")
    private String url;

    // 描述
    @TableField("description")
    private String description;

    // 分类ID
    @TableField("category_id")
    private Long categoryId;

    // 逻辑删除（统一加上！）
    @TableLogic
    private Integer delFlag;
}