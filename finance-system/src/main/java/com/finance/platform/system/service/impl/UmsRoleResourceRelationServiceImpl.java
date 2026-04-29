package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsRoleResourceRelation;
import com.finance.platform.system.mapper.UmsRoleResourceRelationMapper;
import com.finance.platform.system.service.UmsRoleResourceRelationService;
import org.springframework.stereotype.Service;

/**
 * 后台角色资源关系表业务层实现类。
 */
@Service
public class UmsRoleResourceRelationServiceImpl extends ServiceImpl<UmsRoleResourceRelationMapper, UmsRoleResourceRelation>
    implements UmsRoleResourceRelationService {

    /**
     * 默认构造器。
     */
    public UmsRoleResourceRelationServiceImpl() {
    }
}

