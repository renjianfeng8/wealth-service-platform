package com.wealth.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.dto.LoginDTO;
import com.wealth.user.entity.User;
import com.wealth.user.mapper.UserMapper;
import com.wealth.user.service.UserService;
import com.wealth.user.vo.LoginVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;

    public UserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("用户名/密码不能为空");
        }
        user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
        return this.save(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new RuntimeException("用户名/密码不能为空");
        }

        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!PASSWORD_ENCODER.matches(dto.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return new LoginVO(jwtUtil.generateToken(dbUser.getUsername()), dbUser.getId(), dbUser.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(User user) {
        if (user.getId() == null || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("用户ID/新密码不能为空");
        }
        return this.lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getPassword, PASSWORD_ENCODER.encode(user.getPassword()))
                .update();
    }
}
