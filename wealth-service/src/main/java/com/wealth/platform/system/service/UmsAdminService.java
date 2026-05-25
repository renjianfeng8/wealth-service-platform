package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.platform.system.entity.UmsAdmin;

import java.util.List;

public interface UmsAdminService extends IService<UmsAdmin> {
    TokenPair login(LoginDTO dto);
    TokenPair refreshToken(String refreshToken);
    Boolean createAdmin(UmsAdmin admin);
    Boolean updateAdmin(UmsAdmin admin);

    // 权限查询
    List<String> getResourceUrlsByIds(List<Long> resourceIds);

    // 校验指定用户是否有权访问指定 URI
    boolean hasPermission(Long adminId, String uri);

    // 分页条件查询
    IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);
}