package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsResourceMapper;
import com.wealth.platform.system.service.UmsResourceService;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class UmsResourceServiceImpl extends ServiceImpl<UmsResourceMapper, UmsResource> implements UmsResourceService {

    // UmsResourceServiceImpl 实现
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