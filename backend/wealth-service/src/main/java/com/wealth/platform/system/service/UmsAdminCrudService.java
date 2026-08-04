package com.wealth.platform.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.vo.UmsAdminVO;

import java.util.List;

/**
 * 管理员实体 CRUD 与查询服务，只负责 ums_admin 表的数据读写。
 * 认证/令牌生命周期由 {@link UmsAdminAuthService} 承担，权限判定由 {@link PermissionQueryService} 承担。
 */
public interface UmsAdminCrudService extends IService<UmsAdmin> {

    UmsAdminVO getAdminById(Long id);

    List<UmsAdminVO> getAdminList(Integer pageNum, Integer pageSize);

    Boolean createAdmin(UmsAdminDTO dto);

    boolean updateAdmin(Long id, UmsAdminDTO dto);

    boolean deleteAdmin(Long id);

    // 分页条件查询
    IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);

    // 按用户名查询未删除管理员（供认证/权限服务复用）
    UmsAdmin getActiveByUsername(String username);
}
