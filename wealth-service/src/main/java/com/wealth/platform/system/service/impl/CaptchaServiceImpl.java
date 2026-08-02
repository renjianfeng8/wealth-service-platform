package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.constant.CaptchaConstant;
import com.wealth.platform.system.service.CaptchaService;
import com.wealth.platform.system.vo.CaptchaVO;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码实现：生成 SpecCaptcha 图片并写入 Redis（TTL 由 {@link CaptchaConstant} 统一定义）；
 * 校验时读取并消费。Redis 不可用时降级为"生成不持久化 / 校验跳过"，不阻塞主流程。
 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final int CAPTCHA_WIDTH = 130;
    private static final int CAPTCHA_HEIGHT = 48;
    private static final int CAPTCHA_LENGTH = 4;

    private final RedisUtil redisUtil;

    @Override
    public CaptchaVO generate() {
        SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_LENGTH);
        String key = UUID.randomUUID().toString().replace("-", "");
        redisUtil.safeExecuteVoid(() -> redisUtil.set(
                CaptchaConstant.KEY_CAPTCHA + key, captcha.text(),
                CaptchaConstant.CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES), "验证码未持久化");
        return new CaptchaVO(key, captcha.toBase64());
    }

    @Override
    public void verify(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new ServiceException(400, "验证码不能为空");
        }
        String redisKey = CaptchaConstant.KEY_CAPTCHA + captchaKey;
        // Redis 不可用时降级跳过校验（不阻塞登录）
        Boolean keyExists = redisUtil.safeExecute(() -> redisUtil.hasKey(redisKey), null, "跳过验证码校验");
        if (keyExists == null) {
            return;
        }
        String stored = Boolean.TRUE.equals(keyExists) ? (String) redisUtil.get(redisKey) : null;
        if (stored == null) {
            throw new ServiceException(400, "验证码已过期，请重新获取");
        }
        redisUtil.delete(redisKey);
        if (!stored.equalsIgnoreCase(captchaCode.trim())) {
            throw new ServiceException(400, "验证码错误");
        }
    }
}
