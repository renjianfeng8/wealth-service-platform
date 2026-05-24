package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import java.util.List;

/**
 * 后台用户和角色关系表业务层接口。
 */
public interface UmsAdminRoleRelationService extends IService<UmsAdminRoleRelation> {

    // 根据管理员id获取所有角色id
    List<Long> getRoleIdByAdminId(Long adminId);
}

