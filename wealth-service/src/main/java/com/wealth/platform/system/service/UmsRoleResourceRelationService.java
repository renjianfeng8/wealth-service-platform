package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.dto.UmsRoleResourceRelationDTO;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
import java.util.List;

/**
 * 后台角色资源关系表业务层接口。
 */
public interface UmsRoleResourceRelationService extends IService<UmsRoleResourceRelation> {

    // 根据角色id列表，获取所有资源id
    List<Long> getResourceIdByRoleIds(List<Long> roleIds);

    // 分页条件查询
    IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId);

    UmsRoleResourceRelationVO getRoleResourceRelationById(Long id);

    boolean createRelation(UmsRoleResourceRelationDTO dto);

    boolean updateRelation(Long id, UmsRoleResourceRelationDTO dto);

    boolean deleteRelation(Long id);
}

