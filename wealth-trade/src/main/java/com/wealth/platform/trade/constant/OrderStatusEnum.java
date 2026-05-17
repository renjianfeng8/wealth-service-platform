package com.wealth.platform.trade.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    SUBMITTED(1, "已提交"),
    MATCHED(2, "已成交"),
    CANCELLED(3, "已撤销");

    private final int code;
    private final String desc;

    /**
     * 合法状态转换表。
     */
    private static final Set<int[]> VALID_TRANSITIONS = Set.of(
            new int[]{SUBMITTED.code, MATCHED.code},   // 已提交 → 已成交
            new int[]{SUBMITTED.code, CANCELLED.code}   // 已提交 → 已撤销
    );

    public static boolean isValidTransition(int from, int to) {
        return VALID_TRANSITIONS.stream()
                .anyMatch(t -> t[0] == from && t[1] == to);
    }

    public static boolean isValidStatus(int code) {
        for (OrderStatusEnum s : values()) {
            if (s.code == code) return true;
        }
        return false;
    }
}
