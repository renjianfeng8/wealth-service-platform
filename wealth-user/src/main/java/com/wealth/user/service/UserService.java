package com.wealth.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.user.entity.User;
import com.wealth.user.vo.LoginVO;

public interface UserService extends IService<User> {

    // 鐢ㄦ埛娉ㄥ唽
    Boolean register(User user);

    // 鐢ㄦ埛鐧诲綍
    LoginVO login(LoginDTO dto);

    // 閲嶇疆瀵嗙爜
    Boolean resetPassword(User user);
}