package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.mapper.UmsRoleMapper;
import com.wealth.platform.system.service.UmsRoleService;
import com.wealth.platform.system.vo.UmsRoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UmsRoleServiceImpl extends BaseBizServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

    @Override
    public IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByAsc(UmsRole::getSort),
                like(UmsRole::getName, name), eq(UmsRole::getStatus, status));
    }

    @Override
    public UmsRoleVO getRoleById(Long id) {
        return getVoByIdOrThrow(id, UmsRoleVO.class, "角色");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(UmsRoleDTO dto) {
        return save(BeanConvertUtil.convert(dto, UmsRole.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long id, UmsRoleDTO dto) {
        return updateDto(id, dto, "角色");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        return deleteWithCheck(id, "角色");
    }
}