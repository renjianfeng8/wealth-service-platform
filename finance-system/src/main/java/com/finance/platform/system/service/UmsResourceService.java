package com.finance.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.system.entity.UmsResource;
import java.util.List;

/**
 * 后台资源表业务层接口。
 */
public interface UmsResourceService extends IService<UmsResource> {
    // 在 UmsResourceService 加
    List<String> getUrlByResourceIds(List<Long> resourceIds);
}

