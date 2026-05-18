package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.platform.system.entity.UmsAdmin;

import java.util.List;

public interface UmsAdminService extends IService<UmsAdmin> {
    String login(LoginDTO dto);
    Boolean createAdmin(UmsAdmin admin);
    Boolean updateAdmin(UmsAdmin admin);

    // 权限查询
    List<String> getResourceUrlsByIds(List<Long> resourceIds);
}