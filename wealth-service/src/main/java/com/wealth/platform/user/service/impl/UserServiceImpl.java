package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.service.UserService;
import com.wealth.platform.user.vo.LoginVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UmsAdminService umsAdminService;

    public UserServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder, UmsAdminService umsAdminService) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.umsAdminService = umsAdminService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.save(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }

        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (dbUser == null) {
            throw new ServiceException(401, "用户不存在");
        }

        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            throw new ServiceException(401, "账号已被禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), dbUser.getPassword())) {
            throw new ServiceException(401, "密码错误");
        }

        return new LoginVO(jwtUtil.generateToken(dbUser.getUsername()), dbUser.getId(), dbUser.getNickname(), "user");
    }

    @Override
    public LoginVO identifyLogin(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }

        // 1. 先查 ums_admin 表 — 判断是否为管理员
        UmsAdmin admin = umsAdminService.lambdaQuery()
                .eq(UmsAdmin::getUsername, dto.getUsername())
                .one();

        if (admin != null) {
            if (admin.getStatus() != null && admin.getStatus() == 0) {
                throw new ServiceException(401, "账号已被禁用");
            }
            if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
                throw new ServiceException(401, "密码错误");
            }
            String token = jwtUtil.generateToken(admin.getUsername(), "admin");
            return new LoginVO(token, admin.getId(), admin.getNickName(), "admin");
        }

        // 2. 再查 user 表 — 判断是否为普通用户
        User user = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new ServiceException(401, "账号已被禁用");
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new ServiceException(401, "密码错误");
            }
            String token = jwtUtil.generateToken(user.getUsername(), "user");
            return new LoginVO(token, user.getId(), user.getNickname(), "user");
        }

        // 3. 都没找到
        throw new ServiceException(401, "用户名或密码错误");
    }

    @Override
    public IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(User user) {
        if (user.getId() == null || !StringUtils.hasText(user.getPassword())) {
            throw new ServiceException(400, "用户ID/新密码不能为空");
        }
        return this.lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getPassword, passwordEncoder.encode(user.getPassword()))
                .update();
    }
}
