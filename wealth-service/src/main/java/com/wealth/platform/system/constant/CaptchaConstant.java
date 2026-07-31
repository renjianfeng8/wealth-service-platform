package com.wealth.platform.system.constant;

/**
 * 验证码相关常量。
 * 生成（CaptchaController）与校验（UmsAdminServiceImpl）共用同一 key 前缀与有效期，避免两处定义漂移。
 */
public class CaptchaConstant {

    /** 验证码 Redis key 前缀 */
    public static final String KEY_CAPTCHA = "captcha:";

    /** 验证码有效期（分钟） */
    public static final long CAPTCHA_TTL_MINUTES = 5;

    private CaptchaConstant() {
    }
}
