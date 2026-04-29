package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsAdminRoleRelation;
import com.finance.platform.system.mapper.UmsAdminRoleRelationMapper;
import com.finance.platform.system.service.UmsAdminRoleRelationService;
import org.springframework.stereotype.Service;

/**
 * 后台用户和角色关系表业务层实现类。
 */
@Service
public class UmsAdminRoleRelationServiceImpl extends ServiceImpl<UmsAdminRoleRelationMapper, UmsAdminRoleRelation>
    implements UmsAdminRoleRelationService {

    /**
     * 默认构造器。
     */
    public UmsAdminRoleRelationServiceImpl() {
    }
}

