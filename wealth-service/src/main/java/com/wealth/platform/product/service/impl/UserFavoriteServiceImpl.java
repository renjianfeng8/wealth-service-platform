package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
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
public class UserFavoriteServiceImpl extends BaseBizServiceImpl<UserFavoriteMapper, WeaUserFavorite>
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
        return getVoByIdOrThrow(id, UserFavoriteVO.class, "自选关注");
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
        if (existsBy(new LambdaQueryWrapper<WeaUserFavorite>()
                .eq(WeaUserFavorite::getUserId, dto.getUserId())
                .eq(WeaUserFavorite::getProductCode, dto.getProductCode()))) {
            return false;
        }
        WeaUserFavorite entity = BeanConvertUtil.convert(dto, WeaUserFavorite.class);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFavorite(Long id, UserFavoriteDTO dto) {
        return updateDto(id, dto, "自选关注");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long id) {
        if (getById(id) == null) {
            throw new ServiceException(404, "自选关注不存在");
        }
        // wea_user_favorite 表无 del_flag 列，使用物理删除
        return baseMapper.deletePhysicalById(id) > 0;
    }
}
