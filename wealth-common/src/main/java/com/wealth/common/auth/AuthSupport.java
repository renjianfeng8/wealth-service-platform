package com.wealth.common.auth;

import com.wealth.common.exception.ServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * 登录认证共享工具，统一登录参数校验与账号/密码校验语义（单一来源，消除三登录入口重复实现）。
 */
public final class AuthSupport {

    private AuthSupport() {
    }

    /** 登录参数非空校验，缺失抛 400。 */
    public static void assertCredentialsPresent(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }
    }

    /**
     * 校验账号状态与密码：
     * status==0（禁用）抛 401「账号已被禁用」；
     * 密码错误抛 401「用户名或密码错误」（统一模糊文案，防账号枚举）。
     */
    public static void verifyCredentials(BCryptPasswordEncoder encoder, Integer status,
                                         String encodedPassword, String rawPassword) {
        if (status != null && status == 0) {
            throw new ServiceException(401, "账号已被禁用");
        }
        if (!encoder.matches(rawPassword, encodedPassword)) {
            throw new ServiceException(401, "用户名或密码错误");
        }
    }
}
