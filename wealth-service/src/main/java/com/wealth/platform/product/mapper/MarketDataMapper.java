package com.wealth.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.product.entity.WeaMarketData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 行情数据表数据访问层。
 */
@Mapper
public interface MarketDataMapper extends BaseMapper<WeaMarketData> {

    /** 查询全部产品价格总和（用于仪表盘总资产） */
    @Select("SELECT COALESCE(SUM(current_price), 0) FROM wea_market_data")
    BigDecimal sumPrice();

    /** 查询最近两条有价格记录的行情数据（用于计算资产变化率） */
    @Select("SELECT current_price FROM wea_market_data WHERE current_price IS NOT NULL ORDER BY create_time DESC LIMIT 2")
    List<BigDecimal> findLatestTwoPrices();
}

