package com.wealth.platform.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.account.dto.FinUserFavoriteDTO;
import com.wealth.platform.account.entity.WeaUserFavorite;
import com.wealth.platform.account.mapper.FinUserFavoriteMapper;
import com.wealth.platform.account.service.FinUserFavoriteService;
import com.wealth.platform.account.vo.FinUserFavoriteVO;
import com.wealth.common.utils.BeanConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinUserFavoriteServiceImpl extends ServiceImpl<FinUserFavoriteMapper, WeaUserFavorite>
        implements FinUserFavoriteService {

    @Override
    public FinUserFavoriteVO getFavoriteById(Long id) {
        WeaUserFavorite entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinUserFavoriteVO vo = BeanConvertUtil.convert(entity, FinUserFavoriteVO.class);
        return vo;
    }

    @Override
    public List<FinUserFavoriteVO> getFavoriteList(Long userId) {
        List<WeaUserFavorite> list;
        if (userId != null) {
            list = lambdaQuery().eq(WeaUserFavorite::getUserId, userId).list();
        } else {
            list = lambdaQuery().last("LIMIT 1000").list();
        }
        return list.stream().map(entity -> {
            FinUserFavoriteVO vo = BeanConvertUtil.convert(entity, FinUserFavoriteVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFavorite(FinUserFavoriteDTO dto) {
        // 检查是否已关注该产品
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
