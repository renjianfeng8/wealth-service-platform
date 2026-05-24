package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.FinUserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.mapper.FinUserFavoriteMapper;
import com.wealth.platform.product.service.FinUserFavoriteService;
import com.wealth.platform.product.vo.FinUserFavoriteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户自选关注表业务层实现（从 wealth-account 迁移合并）
 */
@Service
public class FinUserFavoriteServiceImpl extends ServiceImpl<FinUserFavoriteMapper, WeaUserFavorite>
        implements FinUserFavoriteService {

    @Override
    public FinUserFavoriteVO getFavoriteById(Long id) {
        WeaUserFavorite entity = getById(id);
        if (entity == null) {
            return null;
        }
        return BeanConvertUtil.convert(entity, FinUserFavoriteVO.class);
    }

    @Override
    public List<FinUserFavoriteVO> getFavoriteList(Long userId) {
        List<WeaUserFavorite> list;
        if (userId != null) {
            list = lambdaQuery().eq(WeaUserFavorite::getUserId, userId).list();
        } else {
            list = lambdaQuery().page(new Page<>(1, 1000)).getRecords();
        }
        return list.stream().map(entity -> BeanConvertUtil.convert(entity, FinUserFavoriteVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFavorite(FinUserFavoriteDTO dto) {
        long count = lambdaQuery()
                .eq(WeaUserFavorite::getUserId, dto.getUserId())
                .eq(WeaUserFavorite::getProductCode, dto.getProductCode())
                .count();
        if (count > 0) {
            return false;
        }
        WeaUserFavorite entity = BeanConvertUtil.convert(dto, WeaUserFavorite.class);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFavorite(Long id, FinUserFavoriteDTO dto) {
        WeaUserFavorite entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long id) {
        return removeById(id);
    }
}
