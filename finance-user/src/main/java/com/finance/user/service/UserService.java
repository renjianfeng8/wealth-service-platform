package com.finance.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.common.result.Result;
import com.finance.user.entity.User;

public interface UserService extends IService<User> {
    // 声明 Feign 调用的方法
    Result getUserAndAccountInfo(Long userId);
}