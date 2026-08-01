package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.vo.UmsAdminVO;

import java.util.List;

public interface UmsAdminService extends IService<UmsAdmin> {
    UmsAdminVO getAdminById(Long id);

    boolean updateAdmin(Long id, UmsAdminDTO dto);

    boolean deleteAdmin(Long id);
    TokenPair login(LoginDTO dto);
    TokenPair refreshToken(String authHeader);
    Boolean createAdmin(UmsAdminDTO dto);

    // 按用户名查询未删除管理员（权限校验用）
    UmsAdmin getActiveByUsername(String username);

    // 权限查询
    List<String> getResourceUrlsByIds(List<Long> resourceIds);

    // 校验指定用户是否有权访问指定 URI
    boolean hasPermission(Long adminId, String uri);

    // 校验请求权限（从 Authorization 头解析 token 并校验）
    boolean checkPermission(String uri, String authHeader);

    // 校验已认证 Token 是否有权访问指定 URI（供拦截器复用，权限判定单一来源）
    boolean checkPermissionForToken(String token, String uri);

    // 分页条件查询
    IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);

    // 重置密码
    Boolean resetPassword(Long id, String oldPassword, String newPassword);

    // 退出登录（将 refresh_token 加入黑名单）
    void logout(String authHeader);

    // 清除指定管理员的权限缓存
    void clearPermissionCache(Long adminId);
}