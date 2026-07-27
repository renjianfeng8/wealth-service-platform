package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.LikeUtil;
import com.wealth.platform.product.dto.UserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.mapper.UserFavoriteMapper;
import com.wealth.platform.product.service.UserFavoriteService;
import com.wealth.platform.product.vo.UserFavoriteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户自选关注表业务层实现（从 wealth-account 迁移合并）
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, WeaUserFavorite>
        implements UserFavoriteService {

    @Override
    public IPage<WeaUserFavorite> pageWithFilter(Integer pageNum, Integer pageSize, Long userId, String productCode) {
        Page<WeaUserFavorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WeaUserFavorite> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && userId > 0) {
            wrapper.eq(WeaUserFavorite::getUserId, userId);
        }
        if (StringUtils.hasText(productCode)) {
            wrapper.like(WeaUserFavorite::getProductCode, LikeUtil.escape(productCode));
        }
        wrapper.orderByDesc(WeaUserFavorite::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public UserFavoriteVO getFavoriteById(Long id) {
        WeaUserFavorite entity = getById(id);
        if (entity == null) {
            return null;
        }
        return BeanConvertUtil.convert(entity, UserFavoriteVO.class);
    }

    @Override
    public List<UserFavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        List<WeaUserFavorite> list;
        if (userId != null) {
            list = lambdaQuery().eq(WeaUserFavorite::getUserId, userId).page(new Page<>(pageNum, pageSize)).getRecords();
        } else {
            list = lambdaQuery().page(new Page<>(pageNum, pageSize)).getRecords();
        }
        return list.stream().map(entity -> BeanConvertUtil.convert(entity, UserFavoriteVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFavorite(UserFavoriteDTO dto) {
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
    public boolean updateFavorite(Long id, UserFavoriteDTO dto) {
        WeaUserFavorite entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "favorite not found");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long id) {
        if (getById(id) == null) {
            throw new ServiceException(404, "favorite not found");
        }
        return removeById(id);
    }
}
