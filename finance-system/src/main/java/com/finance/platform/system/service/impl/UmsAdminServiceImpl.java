package com.finance.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.common.dto.LoginDTO;
import com.finance.common.utils.JwtUtil;
import com.finance.platform.system.entity.UmsAdmin;
import com.finance.platform.system.mapper.UmsAdminMapper;
import com.finance.platform.system.service.UmsAdminService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {

    private final JwtUtil jwtUtil;

    public UmsAdminServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new RuntimeException("用户名密码不能为空");
        }

        UmsAdmin admin = lambdaQuery().eq(UmsAdmin::getUsername, dto.getUsername()).one();
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptPwd = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!encryptPwd.equals(admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtil.generateToken(admin.getUsername());
    }

    @Override
    public Boolean createAdmin(UmsAdmin admin) {
        String encryptPwd = DigestUtils.md5DigestAsHex(admin.getPassword().getBytes(StandardCharsets.UTF_8));
        admin.setPassword(encryptPwd);
        return save(admin);
    }

    @Override
    public Boolean updateAdmin(UmsAdmin admin) {
        admin.setPassword(null);
        return updateById(admin);
    }
}