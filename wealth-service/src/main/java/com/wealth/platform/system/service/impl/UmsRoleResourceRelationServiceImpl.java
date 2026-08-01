package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsRoleResourceRelationDTO;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsRoleResourceRelationMapper;
import com.wealth.platform.system.service.PermissionCacheCleaner;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UmsRoleResourceRelationServiceImpl
        extends BaseBizServiceImpl<UmsRoleResourceRelationMapper, UmsRoleResourceRelation>
        implements UmsRoleResourceRelationService {

    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final PermissionCacheCleaner permissionCacheCleaner;

    @Override
    public IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId) {
        return pageWithFilter(pageNum, pageSize, null, eq(UmsRoleResourceRelation::getRoleId, roleId));
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
    @Transactional(rollbackFor = Exception.class)
    public boolean createRelation(UmsRoleResourceRelationDTO dto) {
        boolean saved = save(BeanConvertUtil.convert(dto, UmsRoleResourceRelation.class));
        clearAdminsByRoleId(dto.getRoleId());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRelation(Long id, UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation existing = getEntityOrThrow(id, "角色资源关联");
        Long oldRoleId = existing.getRoleId();
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = updateById(existing);
        clearAdminsByRoleId(oldRoleId);
        if (dto.getRoleId() != null && !dto.getRoleId().equals(oldRoleId)) {
            clearAdminsByRoleId(dto.getRoleId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRelation(Long id) {
        UmsRoleResourceRelation existing = getEntityOrThrow(id, "角色资源关联");
        boolean removed = removeById(id);
        clearAdminsByRoleId(existing.getRoleId());
        return removed;
    }

    /** 清除拥有指定角色的所有管理员的权限缓存（角色-资源变更需反向扩散到所有关联管理员） */
    private void clearAdminsByRoleId(Long roleId) {
        if (roleId == null) return;
        List<Long> adminIds = adminRoleRelationService.getAdminIdByRoleId(roleId);
        for (Long adminId : adminIds) {
            permissionCacheCleaner.clear(adminId);
        }
    }
}