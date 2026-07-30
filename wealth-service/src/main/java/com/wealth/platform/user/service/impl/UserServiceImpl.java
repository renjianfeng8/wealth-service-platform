package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.LikeUtil;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.service.UserService;
import com.wealth.platform.user.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminIdentityProvider adminIdentityProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }
        long count = lambdaQuery().eq(User::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new ServiceException(400, "用户名已存在");
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
        AdminIdentityDTO admin = adminIdentityProvider.findByUsername(dto.getUsername());

        if (admin != null) {
            if (admin.getStatus() != null && admin.getStatus() == 0) {
                throw new ServiceException(401, "账号已被禁用");
            }
            if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
                throw new ServiceException(401, "密码错误");
            }
            String token = jwtUtil.generateToken(admin.getUsername(), "admin");
            return new LoginVO(token, admin.getId(), admin.getNickname(), "admin");
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
            wrapper.like(User::getUsername, LikeUtil.escape(username));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(User user, String oldPassword) {
        if (!StringUtils.hasText(user.getPassword())) {
            throw new ServiceException(400, "新密码不能为空");
        }
        if (!StringUtils.hasText(oldPassword)) {
            throw new ServiceException(400, "旧密码不能为空");
        }

        // 通过 username 查找用户（如果 id 为空），支持前端只传 username 的场景
        User dbUser;
        if (user.getId() != null) {
            dbUser = getById(user.getId());
        } else if (StringUtils.hasText(user.getUsername())) {
            dbUser = lambdaQuery().eq(User::getUsername, user.getUsername()).one();
        } else {
            throw new ServiceException(400, "用户标识不能为空");
        }

        if (dbUser == null) {
            throw new ServiceException(404, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, dbUser.getPassword())) {
            throw new ServiceException(400, "旧密码不正确");
        }

        return this.lambdaUpdate()
                .eq(User::getId, dbUser.getId())
                .set(User::getPassword, passwordEncoder.encode(user.getPassword()))
                .update();
    }
}
