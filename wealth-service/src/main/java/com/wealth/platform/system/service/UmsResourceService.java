package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsResource;
import java.util.List;

/**
 * 后台资源表业务层接口。
 */
public interface UmsResourceService extends IService<UmsResource> {
    // 在UmsResourceService 加
    List<String> getUrlByResourceIds(List<Long> resourceIds);

    // 分页条件查询
    IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url);
}

