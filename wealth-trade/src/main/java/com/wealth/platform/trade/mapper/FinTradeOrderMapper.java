package com.wealth.platform.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易委托单数据访问层。
 */
@Mapper
public interface FinTradeOrderMapper extends BaseMapper<WeaTradeOrder> {
}

