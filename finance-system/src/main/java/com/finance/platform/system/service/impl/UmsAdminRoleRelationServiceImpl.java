package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.system.entity.UmsAdminRoleRelation;
import com.finance.platform.system.mapper.UmsAdminRoleRelationMapper;
import com.finance.platform.system.service.UmsAdminRoleRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsAdminRoleRelationServiceImpl
        extends ServiceImpl<UmsAdminRoleRelationMapper, UmsAdminRoleRelation>
        implements UmsAdminRoleRelationService {

    @Override
    public List<Long> getRoleIdByAdminId(Long adminId) {
        LambdaQueryWrapper<UmsAdminRoleRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsAdminRoleRelation::getAdminId, adminId);
        List<UmsAdminRoleRelation> list = list(wrapper);
        return list.stream().map(UmsAdminRoleRelation::getRoleId).collect(Collectors.toList());
    }
}