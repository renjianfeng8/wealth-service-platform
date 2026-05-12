package com.finance.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.common.dto.LoginDTO;
import com.finance.user.entity.User;
import com.finance.user.vo.LoginVO;

public interface UserService extends IService<User> {

    // 用户注册
    Boolean register(User user);

    // 用户登录
    LoginVO login(LoginDTO dto);

    // 重置密码
    Boolean resetPassword(User user);
}