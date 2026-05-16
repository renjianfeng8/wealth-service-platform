package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsRoleResourceRelationMapper;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsRoleResourceRelationServiceImpl
        extends ServiceImpl<UmsRoleResourceRelationMapper, UmsRoleResourceRelation>
        implements UmsRoleResourceRelationService {

    @Override
    public List<Long> getResourceIdByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<UmsRoleResourceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UmsRoleResourceRelation::getRoleId, roleIds);
        List<UmsRoleResourceRelation> list = list(wrapper);
        return list.stream().map(UmsRoleResourceRelation::getResourceId).collect(Collectors.toList());
    }
}