package com.finance.platform.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.account.entity.FinUserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户自选关注表数据访问层。
 */
@Mapper
public interface FinUserFavoriteMapper extends BaseMapper<FinUserFavorite> {
}

