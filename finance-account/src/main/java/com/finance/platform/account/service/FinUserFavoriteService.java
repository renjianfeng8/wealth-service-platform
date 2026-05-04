package com.finance.platform.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.account.dto.FinUserFavoriteDTO;
import com.finance.platform.account.entity.FinUserFavorite;
import com.finance.platform.account.vo.FinUserFavoriteVO;
import java.util.List;

/**
 * 用户自选关注表业务层接口。
 */
public interface FinUserFavoriteService extends IService<FinUserFavorite> {

    FinUserFavoriteVO getFavoriteById(Long id);

    List<FinUserFavoriteVO> getFavoriteList();

    boolean createFavorite(FinUserFavoriteDTO dto);

    boolean updateFavorite(Long id, FinUserFavoriteDTO dto);

    boolean deleteFavorite(Long id);
}