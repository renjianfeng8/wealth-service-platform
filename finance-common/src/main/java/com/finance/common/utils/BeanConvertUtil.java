package com.finance.common.utils;

import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConvertUtil {

    public static <S, T> T convert(S source, Class<T> targetCls) {
        if (source == null) return null;
        try {
            T target = targetCls.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }

    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetCls) {
        if (sourceList == null || sourceList.isEmpty()) return List.of();
        return sourceList.stream().map(s -> convert(s, targetCls)).collect(Collectors.toList());
    }
}