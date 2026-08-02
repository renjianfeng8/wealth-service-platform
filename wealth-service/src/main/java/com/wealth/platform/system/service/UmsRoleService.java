package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.vo.UmsRoleVO;

import java.util.List;

/**
 * 后台角色表业务层接口。 */
public interface UmsRoleService extends IService<UmsRole> {
    IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status);

    List<UmsRoleVO> getRoleList(Integer pageNum, Integer pageSize);

    UmsRoleVO getRoleById(Long id);

    boolean createRole(UmsRoleDTO dto);

    boolean updateRole(Long id, UmsRoleDTO dto);

    boolean deleteRole(Long id);
}

