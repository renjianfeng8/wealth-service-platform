package com.finance.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.common.feign.AccountFeignClient;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.user.entity.User;
import com.finance.user.mapper.UserMapper;
import com.finance.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final AccountFeignClient accountFeignClient;

    // 构造注入 Feign
    public UserServiceImpl(AccountFeignClient accountFeignClient) {
        this.accountFeignClient = accountFeignClient;
    }

    /**
     * 【微服务调用】
     * 获取用户信息 + 调用账户服务获取账户信息
     */
    @Override
    public Result getUserAndAccountInfo(Long userId) {
        // 1. 查询当前用户
        User user = getById(userId);
        if (user == null) {
            // 用你已有的 Result.error 方法
            return Result.error(ResultCode.FAIL.getCode(), "用户不存在");
        }

        // 2. 远程调用 account 服务
        Result accountResult = accountFeignClient.getAccountByUserId(userId);
        if (accountResult.getCode() != ResultCode.SUCCESS.getCode()) {
            return Result.error(ResultCode.FAIL.getCode(), "获取账户信息失败：" + accountResult.getMessage());
        }

        // 3. 统一返回
        return Result.success(accountResult.getData());
    }
}