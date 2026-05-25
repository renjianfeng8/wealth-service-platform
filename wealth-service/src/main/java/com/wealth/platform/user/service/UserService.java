package com.wealth.platform.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.vo.LoginVO;

public interface UserService extends IService<User> {

    // 用户注册
    Boolean register(User user);

    // 用户登录
    LoginVO login(LoginDTO dto);

    // 统一登录（自动识别用户类型）
    LoginVO identifyLogin(LoginDTO dto);

    // 重置密码
    Boolean resetPassword(User user);

    // 分页条件查询
    IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);
}
