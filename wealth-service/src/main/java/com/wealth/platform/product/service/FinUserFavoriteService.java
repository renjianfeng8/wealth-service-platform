package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.FinUserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.vo.FinUserFavoriteVO;

import java.util.List;

/**
 * 用户自选关注表业务层接口（从 wealth-account 迁移合并）
 */
public interface FinUserFavoriteService extends IService<WeaUserFavorite> {

    FinUserFavoriteVO getFavoriteById(Long id);

    List<FinUserFavoriteVO> getFavoriteList(Long userId);

    boolean createFavorite(FinUserFavoriteDTO dto);

    boolean updateFavorite(Long id, FinUserFavoriteDTO dto);

    boolean deleteFavorite(Long id);
}
