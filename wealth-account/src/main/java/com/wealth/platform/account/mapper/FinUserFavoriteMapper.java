package com.wealth.platform.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.account.entity.WeaUserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户自选关注表数据访问层
 */
@Mapper
public interface FinUserFavoriteMapper extends BaseMapper<WeaUserFavorite> {
}

