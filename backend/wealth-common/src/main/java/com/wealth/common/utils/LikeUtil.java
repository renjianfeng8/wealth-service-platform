package com.wealth.common.utils;

/**
 * MyBatis-Plus LIKE 查询通配符转义工具。
 * MySQL LIKE 中 % 和 _ 是通配符，需转义为 \% 和 \_，
 * 并使用 ESCAPE '/' 语法（MySQL 默认 ESCAPE '\'）。
 */
public class LikeUtil {

    /**
     * 转义 LIKE 通配符 % 和 _，防止意外匹配。
     */
    public static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value
                .replace("\\", "\\\\")   // 反斜杠自身先转义
                .replace("_", "\\_")
                .replace("%", "\\%");
    }
}
