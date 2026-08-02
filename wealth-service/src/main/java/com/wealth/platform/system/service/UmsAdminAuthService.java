package com.wealth.platform.system.service;

import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.JwtUtil.TokenPair;

/**
 * 管理员认证与令牌生命周期服务：登录、刷新、退出、重置密码。
 * 不依赖具体存储，通过 {@link UmsAdminCrudService} 访问 ums_admin 数据。
 */
public interface UmsAdminAuthService {

    TokenPair login(LoginDTO dto);

    TokenPair refreshToken(String authHeader);

    void logout(String authHeader);

    Boolean resetPassword(Long id, String oldPassword, String newPassword);
}
