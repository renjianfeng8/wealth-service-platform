package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsResource;
import com.finance.platform.system.mapper.UmsResourceMapper;
import com.finance.platform.system.service.UmsResourceService;
import org.springframework.stereotype.Service;

/**
 * 后台资源表业务层实现类。
 */
@Service
public class UmsResourceServiceImpl extends ServiceImpl<UmsResourceMapper, UmsResource>
    implements UmsResourceService {

    /**
     * 默认构造器。
     */
    public UmsResourceServiceImpl() {
    }
}

