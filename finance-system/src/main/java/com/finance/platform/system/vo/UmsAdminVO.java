package com.finance.platform.system.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 后台管理员 VO
 * 统一返回给前端
 */
@Data
public class UmsAdminVO {

    private Long id;

    private String username;

    private String email;

    private String nickName;

    private Integer status;

    private String avatar;

    private LocalDateTime createTime;

    private LocalDateTime loginTime;
}