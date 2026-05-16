package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsResource;
import java.util.List;

/**
 * 鍚庡彴璧勬簮琛ㄤ笟鍔″眰鎺ュ彛銆?
 */
public interface UmsResourceService extends IService<UmsResource> {
    // 鍦?UmsResourceService 鍔?
    List<String> getUrlByResourceIds(List<Long> resourceIds);
}

