package com.wealth.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BeanConvertUtil {

    /** 缓存每个类的 null 属性名数组，避免重复反射 */
    private static final Map<Class<?>, String[]> NULL_PROPERTY_CACHE = new ConcurrentHashMap<>();

    public static <S, T> T convert(S source, Class<T> targetCls) {
        if (source == null) return null;
        try {
            T target = targetCls.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new com.wealth.common.exception.ServiceException(500, "转换失败", e);
        }
    }

    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetCls) {
        if (sourceList == null || sourceList.isEmpty()) return new ArrayList<>();
        return sourceList.stream().map(s -> convert(s, targetCls)).collect(Collectors.toList());
    }

    /**
     * 将 Entity 分页对象转换为 VO 分页对象，包括分页属性和记录转换。
     */
    public static <S, T> IPage<T> convertPage(IPage<S> sourcePage, Class<T> targetCls) {
        Page<T> voPage = new Page<>();
        voPage.setCurrent(sourcePage.getCurrent());
        voPage.setSize(sourcePage.getSize());
        voPage.setTotal(sourcePage.getTotal());
        voPage.setPages(sourcePage.getPages());
        voPage.setRecords(convertList(sourcePage.getRecords(), targetCls));
        return voPage;
    }

    /** 复制非 null 属性，避免覆盖目标对象的已有值 */
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static String[] getNullPropertyNames(Object source) {
        Class<?> sourceClass = source.getClass();
        // 先尝试从缓存获取
        String[] cached = NULL_PROPERTY_CACHE.get(sourceClass);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中，反射计算
        try {
            Set<String> emptyNames = new HashSet<>();
            for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(sourceClass)) {
                if (pd.getReadMethod() != null && pd.getReadMethod().invoke(source) == null) {
                    emptyNames.add(pd.getName());
                }
            }
            String[] result = emptyNames.toArray(new String[0]);
            NULL_PROPERTY_CACHE.put(sourceClass, result);
            return result;
        } catch (Exception e) {
            throw new com.wealth.common.exception.ServiceException(500, "获取 null 属性名失败", e);
        }
    }
}
