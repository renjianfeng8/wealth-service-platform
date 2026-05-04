package com.finance.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UmsRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer delFlag;
}