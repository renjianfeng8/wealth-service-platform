package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.UserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.vo.UserFavoriteVO;

import java.util.List;

/**
 * 用户自选关注表业务层接口（从 wealth-account 迁移合并）
 */
public interface UserFavoriteService extends IService<WeaUserFavorite> {

    IPage<WeaUserFavorite> pageWithFilter(Integer pageNum, Integer pageSize, Long userId, String productCode);

    UserFavoriteVO getFavoriteById(Long id);

    List<UserFavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize);

    boolean createFavorite(UserFavoriteDTO dto);

    boolean updateFavorite(Long id, UserFavoriteDTO dto);

    boolean deleteFavorite(Long id);
}
