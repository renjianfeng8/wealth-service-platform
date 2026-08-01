package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsResourceMapper;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.vo.UmsResourceVO;
import com.wealth.common.utils.LikeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class UmsResourceServiceImpl extends BaseBizServiceImpl<UmsResourceMapper, UmsResource> implements UmsResourceService {

    @Override
    public IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(UmsResource::getName, LikeUtil.escape(name));
        }
        if (StringUtils.hasText(url)) {
            wrapper.like(UmsResource::getUrl, LikeUtil.escape(url));
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

    @Override
    public UmsResourceVO getResourceById(Long id) {
        return getVoByIdOrThrow(id, UmsResourceVO.class, "后台资源");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResource(Long id, UmsResourceDTO dto) {
        return updateDto(id, dto, "后台资源");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResource(Long id) {
        return deleteWithCheck(id, "后台资源");
    }
}