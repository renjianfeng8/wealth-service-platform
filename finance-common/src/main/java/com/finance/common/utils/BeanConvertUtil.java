package com.finance.common.utils;

import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体、DTO、VO 转换工具类
 */
public class BeanConvertUtil {

    /**
     * 单个对象转换
     * @param source 源对象
     * @param targetCls 目标类
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, Class<T> targetCls) {
        if (source == null) {
            return null;
        }
        T target;
        try {
            target = targetCls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败", e);
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 集合 List 转换
     * @param sourceList 源集合
     * @param targetCls 目标类
     * @return 转换后的 List
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetCls) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList.stream()
                .map(source -> convert(source, targetCls))
                .collect(Collectors.toList());
    }
}