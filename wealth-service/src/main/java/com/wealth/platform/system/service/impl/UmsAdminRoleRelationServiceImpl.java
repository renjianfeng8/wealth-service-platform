package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.mapper.UmsAdminRoleRelationMapper;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsAdminRoleRelationServiceImpl
        extends BaseBizServiceImpl<UmsAdminRoleRelationMapper, UmsAdminRoleRelation>
        implements UmsAdminRoleRelationService {

    @Override
    public IPage<UmsAdminRoleRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long adminId) {
        return pageWithFilter(pageNum, pageSize, null, eq(UmsAdminRoleRelation::getAdminId, adminId));
    }

    @Override
    public List<Long> getRoleIdByAdminId(Long adminId) {
        LambdaQueryWrapper<UmsAdminRoleRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsAdminRoleRelation::getAdminId, adminId);
        return listColumn(wrapper, UmsAdminRoleRelation::getRoleId);
    }

    @Override
    public List<Long> getAdminIdByRoleId(Long roleId) {
        LambdaQueryWrapper<UmsAdminRoleRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsAdminRoleRelation::getRoleId, roleId);
        return listColumn(wrapper, UmsAdminRoleRelation::getAdminId, true);
    }

    @Override
    public UmsAdminRoleRelationVO getAdminRoleRelationById(Long id) {
        return getVoByIdOrThrow(id, UmsAdminRoleRelationVO.class, "管理员角色关联");
    }

    @Override
    public UmsAdminRoleRelation getAdminRoleRelationEntityOrThrow(Long id) {
        return getEntityOrThrow(id, "管理员角色关联");
    }
}