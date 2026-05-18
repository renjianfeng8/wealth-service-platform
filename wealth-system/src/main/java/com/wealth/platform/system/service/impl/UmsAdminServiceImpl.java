package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {

    private final JwtUtil jwtUtil;
    private final UmsResourceService resourceService;
    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final UmsRoleResourceRelationService roleResourceRelationService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UmsAdminServiceImpl(JwtUtil jwtUtil, UmsResourceService resourceService,
                               UmsAdminRoleRelationService adminRoleRelationService,
                               UmsRoleResourceRelationService roleResourceRelationService,
                               BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.resourceService = resourceService;
        this.adminRoleRelationService = adminRoleRelationService;
        this.roleResourceRelationService = roleResourceRelationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(401, "用户名密码不能为空");
        }

        UmsAdmin admin = lambdaQuery()
                .eq(UmsAdmin::getUsername, dto.getUsername())
                .one();

        if (admin == null) {
            throw new ServiceException(401, "用户不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new ServiceException(401, "密码错误");
        }

        return jwtUtil.generateToken(admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAdmin(UmsAdmin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return save(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAdmin(UmsAdmin admin) {
        admin.setPassword(null);
        return updateById(admin);
    }

    // ================== 权限查询方法 ==================
    @Override
    public List<String> getResourceUrlsByIds(List<Long> resourceIds) {
        return resourceService.lambdaQuery()
                .in(UmsResource::getId, resourceIds)
                .list()
                .stream()
                .map(UmsResource::getUrl)
                .collect(Collectors.toList());
    }

    /** 判断指定 admin 是否有权访问某 URI（Ant 风格匹配） */
    @Override
    public boolean hasPermission(Long adminId, String uri) {
        // 1. 查询用户的角色ID列表
        List<Long> roleIds = adminRoleRelationService.getRoleIdByAdminId(adminId);
        if (roleIds.isEmpty()) return false;

        // 2. 查询角色拥有的资源ID列表
        List<Long> resourceIds = roleResourceRelationService.getResourceIdByRoleIds(roleIds);
        if (resourceIds.isEmpty()) return false;

        // 3. 查询资源对应的 URL 表达式
        List<String> urlPatterns = getResourceUrlsByIds(resourceIds);

        // 4. Ant 风格路径匹配
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return urlPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
