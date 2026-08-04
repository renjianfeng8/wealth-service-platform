package com.wealth.platform.system.service;

import com.wealth.platform.system.vo.CaptchaVO;

/**
 * 验证码生成与校验服务：统一承载验证码的生成、Redis 存储与校验，
 * 避免 Controller 直接操作缓存、验证码逻辑在展示层漂移。
 */
public interface CaptchaService {

    CaptchaVO generate();

    void verify(String captchaKey, String captchaCode);
}
