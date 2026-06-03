package com.wealth.common.dto;

import lombok.Data;

/**
 * 管理员登录身份快照。
 */
@Data
public class AdminIdentityDTO {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Integer status;
}
