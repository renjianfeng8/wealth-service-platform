package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsRole;
import com.finance.platform.system.mapper.UmsRoleMapper;
import com.finance.platform.system.service.UmsRoleService;
import org.springframework.stereotype.Service;

/**
 * 后台角色表业务层实现类。
 */
@Service
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole>
    implements UmsRoleService {

    /**
     * 默认构造器。
     */
    public UmsRoleServiceImpl() {
    }
}

