package com.wealth.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重放注解。
 * 标注在 Controller 方法上，要求请求携带 X-Timestamp 和 X-Nonce 头，
 * 由 AntiReplayAspect 校验时间窗口和 nonce 唯一性，防止请求被抓包重放。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AntiReplay {

    /** 时间窗口（秒），默认 300 秒（5 分钟） */
    int timeWindow() default 300;
}
