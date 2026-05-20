package com.wealth.platform.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.account.dto.FinUserFavoriteDTO;
import com.wealth.platform.account.entity.WeaUserFavorite;
import com.wealth.platform.account.vo.FinUserFavoriteVO;
import java.util.List;

/**
 * 用户自选关注表业务层接口
 */
public interface FinUserFavoriteService extends IService<WeaUserFavorite> {

    FinUserFavoriteVO getFavoriteById(Long id);

    List<FinUserFavoriteVO> getFavoriteList(Long userId);

    boolean createFavorite(FinUserFavoriteDTO dto);

    boolean updateFavorite(Long id, FinUserFavoriteDTO dto);

    boolean deleteFavorite(Long id);
}