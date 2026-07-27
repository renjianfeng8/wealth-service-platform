package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.mapper.UmsRoleMapper;
import com.wealth.platform.system.service.UmsRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.wealth.common.utils.LikeUtil;

@Service
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

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
}