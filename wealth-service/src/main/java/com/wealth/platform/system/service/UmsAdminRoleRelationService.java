package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import java.util.List;

/**
 * 后台用户和角色关系表业务层接口。
 */
public interface UmsAdminRoleRelationService extends IService<UmsAdminRoleRelation> {

    // 根据管理员id获取所有角色id
    List<Long> getRoleIdByAdminId(Long adminId);

    // 根据角色id获取所有管理员id（用于缓存失效）
    List<Long> getAdminIdByRoleId(Long roleId);

    // 分页条件查询
    IPage<UmsAdminRoleRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long adminId);
}

