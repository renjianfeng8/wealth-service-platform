package com.wealth.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 鍚庡彴瑙掕壊琛ㄣ€? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_role")
public class UmsRole extends BaseEntity {

    private String name;

    private String description;

    private Integer status;

    private Integer sort;

    /** ums_role 琛ㄦ棤 update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
