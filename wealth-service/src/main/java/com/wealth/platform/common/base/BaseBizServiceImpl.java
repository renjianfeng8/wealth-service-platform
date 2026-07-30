package com.wealth.platform.common.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.common.entity.BaseEntity;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;

import java.util.List;
import java.util.stream.Collectors;

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
