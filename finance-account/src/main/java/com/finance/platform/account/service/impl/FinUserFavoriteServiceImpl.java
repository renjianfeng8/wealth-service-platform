package com.finance.platform.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.account.dto.FinUserFavoriteDTO;
import com.finance.platform.account.entity.FinUserFavorite;
import com.finance.platform.account.mapper.FinUserFavoriteMapper;
import com.finance.platform.account.service.FinUserFavoriteService;
import com.finance.platform.account.vo.FinUserFavoriteVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinUserFavoriteServiceImpl extends ServiceImpl<FinUserFavoriteMapper, FinUserFavorite>
        implements FinUserFavoriteService {

    @Override
    public FinUserFavoriteVO getFavoriteById(Long id) {
        FinUserFavorite entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinUserFavoriteVO vo = new FinUserFavoriteVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinUserFavoriteVO> getFavoriteList() {
        List<FinUserFavorite> list = list();
        return list.stream().map(entity -> {
            FinUserFavoriteVO vo = new FinUserFavoriteVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFavorite(FinUserFavoriteDTO dto) {
        // 检查是否已关注该产品
        long count = lambdaQuery()
                .eq(FinUserFavorite::getUserId, dto.getUserId())
                .eq(FinUserFavorite::getProductCode, dto.getProductCode())
                .count();
        if (count > 0) {
            return false;
        }
        FinUserFavorite entity = new FinUserFavorite();
        BeanUtils.copyProperties(dto, entity);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFavorite(Long id, FinUserFavoriteDTO dto) {
        FinUserFavorite entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long id) {
        return removeById(id);
    }
}