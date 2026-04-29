package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsAdmin;
import com.finance.platform.system.mapper.UmsAdminMapper;
import com.finance.platform.system.service.UmsAdminService;
import org.springframework.stereotype.Service;

/**
 * 后台管理员表业务层实现类。
 */
@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin>
    implements UmsAdminService {

    /**
     * 默认构造器。
     */
    public UmsAdminServiceImpl() {
    }
}

