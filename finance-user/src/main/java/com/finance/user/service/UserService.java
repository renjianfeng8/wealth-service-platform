package com.finance.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.user.entity.User;

public interface UserService extends IService<User> {

    // 用户注册
    Boolean register(User user);

    // 用户登录
    String login(User user);

    // 重置密码
    Boolean resetPassword(User user);
}