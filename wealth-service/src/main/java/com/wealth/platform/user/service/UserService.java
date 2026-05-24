package com.wealth.platform.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.vo.LoginVO;

public interface UserService extends IService<User> {

    // 用户注册
    Boolean register(User user);

    // 用户登录
    LoginVO login(LoginDTO dto);

    // 重置密码
    Boolean resetPassword(User user);
}
