package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.UmsAdminCrudService;
import com.wealth.platform.system.vo.UmsAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员 CRUD 与身份查询实现。同时承担 {@link AdminIdentityProvider} 身份读取契约，
 * 供 user 域统一登录识别管理员身份。
 */
@Service
@RequiredArgsConstructor
public class UmsAdminCrudServiceImpl extends BaseBizServiceImpl<UmsAdminMapper, UmsAdmin>
        implements UmsAdminCrudService, AdminIdentityProvider {

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UmsAdminVO getAdminById(Long id) {
        return getVoByIdOrThrow(id, UmsAdminVO.class, "管理员");
    }

    @Override
    public List<UmsAdminVO> getAdminList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, UmsAdminVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAdmin(UmsAdminDTO dto) {
        UmsAdmin admin = BeanConvertUtil.convert(dto, UmsAdmin.class);
        checkUnique(UmsAdmin::getUsername, admin.getUsername(), "管理员用户名已存在");
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        try {
            return save(admin);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException(400, "管理员用户名已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(Long id, UmsAdminDTO dto) {
        UmsAdmin entity = getEntityOrThrow(id, "管理员");
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setPassword(null);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdmin(Long id) {
        return deleteWithCheck(id, "管理员");
    }

    @Override
    public IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(UmsAdmin::getCreateTime),
                like(UmsAdmin::getUsername, username), eq(UmsAdmin::getStatus, status));
    }

    @Override
    public UmsAdmin getActiveByUsername(String username) {
        return lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();
    }

    @Override
    public AdminIdentityDTO findByUsername(String username) {
        UmsAdmin admin = getActiveByUsername(username);
        if (admin == null) {
            return null;
        }
        AdminIdentityDTO dto = BeanConvertUtil.convert(admin, AdminIdentityDTO.class);
        dto.setNickname(admin.getNickName());
        return dto;
    }
}
