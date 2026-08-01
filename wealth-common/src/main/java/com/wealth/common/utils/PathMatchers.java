package com.wealth.common.utils;

import org.springframework.util.AntPathMatcher;

/** 全局共享 AntPathMatcher：线程安全、无状态，收敛各模块重复实例化 */
public final class PathMatchers {

    private PathMatchers() {
    }

    public static final AntPathMatcher INSTANCE = new AntPathMatcher();
}
