package com.finance.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.common.dto.LoginDTO;
import com.finance.platform.system.entity.UmsAdmin;

public interface UmsAdminService extends IService<UmsAdmin> {
    String login(LoginDTO dto);
    Boolean createAdmin(UmsAdmin admin);
    Boolean updateAdmin(UmsAdmin admin);
}