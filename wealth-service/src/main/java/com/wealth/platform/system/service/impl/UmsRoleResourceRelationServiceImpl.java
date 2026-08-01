package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsRoleResourceRelationMapper;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsRoleResourceRelationServiceImpl
        extends BaseBizServiceImpl<UmsRoleResourceRelationMapper, UmsRoleResourceRelation>
        implements UmsRoleResourceRelationService {

    @Override
    public IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId) {
        Page<UmsRoleResourceRelation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsRoleResourceRelation> wrapper = new LambdaQueryWrapper<>();
        if (roleId != null) {
            wrapper.eq(UmsRoleResourceRelation::getRoleId, roleId);
        }
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Long> getResourceIdByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<UmsRoleResourceRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UmsRoleResourceRelation::getRoleId, roleIds);
        return listColumn(wrapper, UmsRoleResourceRelation::getResourceId);
    }

    @Override
    public UmsRoleResourceRelationVO getRoleResourceRelationById(Long id) {
        return getVoByIdOrThrow(id, UmsRoleResourceRelationVO.class, "角色资源关联");
    }

    @Override
    public UmsRoleResourceRelation getRoleResourceRelationEntityOrThrow(Long id) {
        return getEntityOrThrow(id, "角色资源关联");
    }
}