package com.wealth.platform.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.account.dto.FinUserFavoriteDTO;
import com.wealth.platform.account.entity.WeaUserFavorite;
import com.wealth.platform.account.vo.FinUserFavoriteVO;
import java.util.List;

/**
 * 鐢ㄦ埛鑷€夊叧娉ㄨ〃涓氬姟灞傛帴鍙ｃ€?
 */
public interface FinUserFavoriteService extends IService<WeaUserFavorite> {

    FinUserFavoriteVO getFavoriteById(Long id);

    List<FinUserFavoriteVO> getFavoriteList();

    boolean createFavorite(FinUserFavoriteDTO dto);

    boolean updateFavorite(Long id, FinUserFavoriteDTO dto);

    boolean deleteFavorite(Long id);
}