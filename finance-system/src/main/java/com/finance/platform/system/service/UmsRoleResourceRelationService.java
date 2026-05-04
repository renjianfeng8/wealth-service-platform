package com.finance.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.system.entity.UmsRoleResourceRelation;
import java.util.List;

/**
 * 后台角色资源关系表业务层接口。
 */
public interface UmsRoleResourceRelationService extends IService<UmsRoleResourceRelation> {

    // 根据角色id列表，获取所有资源id
    List<Long> getResourceIdByRoleIds(List<Long> roleIds);
}

