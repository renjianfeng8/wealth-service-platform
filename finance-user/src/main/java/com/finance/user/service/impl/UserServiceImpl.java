package com.finance.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.user.entity.User;
import com.finance.user.mapper.UserMapper;
import com.finance.user.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expire}")
    private Long jwtExpire;

    @Override
    public Boolean register(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("用户名/密码不能为空");
        }
        String encryptPwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(encryptPwd);
        return this.save(user);
    }

    @Override
    public String login(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("用户名/密码不能为空");
        }

        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptPwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!encryptPwd.equals(dbUser.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(dbUser.getId().toString())
                .claim("username", dbUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpire))
                .signWith(key)
                .compact();
    }

    @Override
    public Boolean resetPassword(User user) {
        if (user.getId() == null || !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("用户ID/新密码不能为空");
        }
        String encryptPwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));
        return this.lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getPassword, encryptPwd)
                .update();
    }
}