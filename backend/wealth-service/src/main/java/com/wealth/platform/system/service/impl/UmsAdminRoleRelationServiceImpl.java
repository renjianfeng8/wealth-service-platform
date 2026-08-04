package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsAdminRoleRelationDTO;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.mapper.UmsAdminRoleRelationMapper;
import com.wealth.platform.system.service.PermissionCacheCleaner;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UmsAdminRoleRelationServiceImpl
        extends BaseBizServiceImpl<UmsAdminRoleRelationMapper, UmsAdminRoleRelation>
        implements UmsAdminRoleRelationService {

    private final PermissionCacheCleaner permissionCacheCleaner;

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
    public List<UmsAdminRoleRelationVO> getAdminRoleRelationList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, UmsAdminRoleRelationVO.class);
    }

    @Override
    public UmsAdminRoleRelationVO getAdminRoleRelationById(Long id) {
        return getVoByIdOrThrow(id, UmsAdminRoleRelationVO.class, "管理员角色关联");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRelation(UmsAdminRoleRelationDTO dto) {
        boolean saved = save(BeanConvertUtil.convert(dto, UmsAdminRoleRelation.class));
        permissionCacheCleaner.clear(dto.getAdminId());
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRelation(Long id, UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation existing = getEntityOrThrow(id, "管理员角色关联");
        Long oldAdminId = existing.getAdminId();
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = updateById(existing);
        permissionCacheCleaner.clear(oldAdminId);
        if (dto.getAdminId() != null && !dto.getAdminId().equals(oldAdminId)) {
            permissionCacheCleaner.clear(dto.getAdminId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRelation(Long id) {
        UmsAdminRoleRelation existing = getEntityOrThrow(id, "管理员角色关联");
        boolean removed = removeById(id);
        permissionCacheCleaner.clear(existing.getAdminId());
        return removed;
    }
}