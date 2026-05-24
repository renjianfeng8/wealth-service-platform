package com.wealth.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.product.entity.WeaMarketData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行情数据表数据访问层。
 */
@Mapper
public interface FinMarketDataMapper extends BaseMapper<WeaMarketData> {
}

