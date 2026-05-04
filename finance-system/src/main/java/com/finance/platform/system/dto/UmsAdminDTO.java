package com.finance.platform.system.dto;

import lombok.Data;

/**
 * 管理员新增/修改 DTO
 */
@Data
public class UmsAdminDTO {

    private Long id;

    private String username;

    private String password;

    private String email;

    private String nickName;

    private Integer status;

    private String avatar;
}