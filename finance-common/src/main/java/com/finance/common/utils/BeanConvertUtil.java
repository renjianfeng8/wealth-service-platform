package com.finance.common.utils;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        if (sourceList == null || sourceList.isEmpty()) return new ArrayList<>();
        return sourceList.stream().map(s -> convert(s, targetCls)).collect(Collectors.toList());
    }

    /** 复制非 null 属性，避免覆盖目标对象的已有值 */
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static String[] getNullPropertyNames(Object source) {
        try {
            Set<String> emptyNames = new HashSet<>();
            for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(source.getClass())) {
                if (pd.getReadMethod() != null && pd.getReadMethod().invoke(source) == null) {
                    emptyNames.add(pd.getName());
                }
            }
            return emptyNames.toArray(new String[0]);
        } catch (Exception e) {
            throw new RuntimeException("获取 null 属性名失败", e);
        }
    }
}