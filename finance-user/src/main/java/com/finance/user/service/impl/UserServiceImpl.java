package com.finance.platform.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.user.entity.User;
import com.finance.platform.user.mapper.UserMapper;
import com.finance.platform.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}