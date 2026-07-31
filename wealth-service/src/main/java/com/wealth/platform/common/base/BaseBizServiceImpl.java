package com.wealth.platform.common.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.common.entity.BaseEntity;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 业务层基础实现，收敛各 Service 中重复的 getById→convert / getList / update / delete 模板代码。
 *
 * @param <M> Mapper 类型
 * @param <E> Entity 类型，必须继承 BaseEntity（含 getId/setId）
 */
public abstract class BaseBizServiceImpl<M extends BaseMapper<E>, E extends BaseEntity>
        extends ServiceImpl<M, E> {

    /**
     * 根据 ID 查询并转换为 VO，不存在时返回 null。
     */
    protected <V> V getVoById(Long id, Class<V> voClass) {
        E entity = getById(id);
        return entity == null ? null : BeanConvertUtil.convert(entity, voClass);
    }

    /**
     * 分页查询并转换为 VO 列表。
     */
    protected <V> List<V> pageVoList(Integer pageNum, Integer pageSize, Class<V> voClass) {
        List<E> list = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(e -> BeanConvertUtil.convert(e, voClass)).collect(Collectors.toList());
    }

    /**
     * 查询并提取单列。
     * 收敛关系表"查 id 列表"样板：wrapper.eq/in → list → map(列) → collect。
     *
     * @param wrapper 查询条件
     * @param mapper  实体到目标列的映射
     * @param <R>     目标列类型
     */
    protected <R> List<R> listColumn(Wrapper<E> wrapper, Function<E, R> mapper) {
        return listColumn(wrapper, mapper, false);
    }

    /**
     * 查询并提取单列（可选去重）。
     */
    protected <R> List<R> listColumn(Wrapper<E> wrapper, Function<E, R> mapper, boolean distinct) {
        Stream<R> column = list(wrapper).stream().map(mapper);
        if (distinct) {
            column = column.distinct();
        }
        return column.collect(Collectors.toList());
    }

    /**
     * 校验指定条件是否存在匹配记录。
     *
     * @param wrapper 查询条件
     * @return 存在返回 true
     */
    protected boolean existsBy(LambdaQueryWrapper<E> wrapper) {
        return count(wrapper) > 0;
    }

    /**
     * 唯一性校验：目标列存在相同值时抛 400 异常。
     *
     * @param column  唯一列
     * @param value   待校验值
     * @param message 异常消息
     */
    protected void checkUnique(SFunction<E, ?> column, Object value, String message) {
        if (existsBy(new LambdaQueryWrapper<E>().eq(column, value))) {
            throw new ServiceException(400, message);
        }
    }

    /**
     * 通用更新：先判空再拷贝非空属性后更新。
     *
     * @param id         主键
     * @param dto        DTO 源对象
     * @param entityName 实体中文名（异常消息用）
     * @return 是否更新成功
     */
    protected boolean updateDto(Long id, Object dto, String entityName) {
        E entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, entityName + "不存在");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    /**
     * 通用删除：先判空再删除。
     *
     * @param id         主键
     * @param entityName 实体中文名（异常消息用）
     * @return 是否删除成功
     */
    protected boolean deleteWithCheck(Long id, String entityName) {
        if (getById(id) == null) {
            throw new ServiceException(404, entityName + "不存在");
        }
        return removeById(id);
    }
}
