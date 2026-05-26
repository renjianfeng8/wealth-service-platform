package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsResourceMapper;
import com.wealth.platform.system.service.UmsResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class UmsResourceServiceImpl extends ServiceImpl<UmsResourceMapper, UmsResource> implements UmsResourceService {

    @Override
    public IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(UmsResource::getName, name);
        }
        if (StringUtils.hasText(url)) {
            wrapper.like(UmsResource::getUrl, url);
        }
        wrapper.orderByDesc(UmsResource::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<String> getUrlByResourceIds(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UmsResource::getId, resourceIds);
        List<UmsResource> list = list(wrapper);
        return list.stream().map(UmsResource::getUrl).collect(Collectors.toList());
    }
}