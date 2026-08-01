package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.mapper.UmsRoleMapper;
import com.wealth.platform.system.service.UmsRoleService;
import com.wealth.platform.system.vo.UmsRoleVO;
import com.wealth.common.utils.LikeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UmsRoleServiceImpl extends BaseBizServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

    @Override
    public IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(UmsRole::getName, LikeUtil.escape(name));
        }
        if (status != null) {
            wrapper.eq(UmsRole::getStatus, status);
        }
        wrapper.orderByAsc(UmsRole::getSort);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public UmsRoleVO getRoleById(Long id) {
        return getVoByIdOrThrow(id, UmsRoleVO.class, "角色");
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