package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.common.dto.LoginDTO;
import com.finance.common.utils.JwtUtil;
import com.finance.platform.system.entity.UmsAdmin;
import com.finance.platform.system.entity.UmsResource;
import com.finance.platform.system.mapper.UmsAdminMapper;
import com.finance.platform.system.service.UmsAdminService;
import com.finance.platform.system.service.UmsResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {

    private final JwtUtil jwtUtil;
    private final UmsResourceService resourceService;

    // 注入依赖
    public UmsAdminServiceImpl(JwtUtil jwtUtil, UmsResourceService resourceService) {
        this.jwtUtil = jwtUtil;
        this.resourceService = resourceService;
    }

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public String login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new RuntimeException("用户名密码不能为空");
        }

        UmsAdmin admin = lambdaQuery()
                .eq(UmsAdmin::getUsername, dto.getUsername())
                .one();

        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!PASSWORD_ENCODER.matches(dto.getPassword(), admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtil.generateToken(admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAdmin(UmsAdmin admin) {
        admin.setPassword(PASSWORD_ENCODER.encode(admin.getPassword()));
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
}