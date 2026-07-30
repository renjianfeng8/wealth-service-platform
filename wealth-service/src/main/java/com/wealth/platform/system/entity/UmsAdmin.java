package com.wealth.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后台管理员表。 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("ums_admin")
public class UmsAdmin extends BaseEntity {

    private String username;

    private String password;

    private String email;

    private String nickName;

    private Integer status;

    private String avatar;

    private LocalDateTime loginTime;

}
