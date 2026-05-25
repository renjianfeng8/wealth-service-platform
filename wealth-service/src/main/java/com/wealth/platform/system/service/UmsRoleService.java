package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsRole;

/**
 * 后台角色表业务层接口。 */
public interface UmsRoleService extends IService<UmsRole> {
    IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status);
}

