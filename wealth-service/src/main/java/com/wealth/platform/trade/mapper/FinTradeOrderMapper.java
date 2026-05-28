package com.wealth.platform.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 交易委托单数据访问层。
 */
@Mapper
public interface FinTradeOrderMapper extends BaseMapper<WeaTradeOrder> {

    /** 已成交订单总金额 */
    @Select("SELECT COALESCE(SUM(order_price * order_quantity), 0) FROM wea_trade_order WHERE order_status = 2")
    BigDecimal sumCompletedAmount();

    /** 今日已成交订单总金额 */
    @Select("SELECT COALESCE(SUM(order_price * order_quantity), 0) FROM wea_trade_order WHERE order_status = 2 AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayCompletedAmount();

    /** 按时间升序的前 N 条已成交订单金额总和（用于趋势对比） */
    @Select("SELECT COALESCE(SUM(t.amount), 0) FROM (SELECT order_price * order_quantity AS amount FROM wea_trade_order WHERE order_status = 2 ORDER BY create_time ASC LIMIT #{limit}) t")
    BigDecimal sumFirstHalf(@Param("limit") int limit);

    /** 按时间降序的前 N 条已成交订单金额总和（用于趋势对比） */
    @Select("SELECT COALESCE(SUM(t.amount), 0) FROM (SELECT order_price * order_quantity AS amount FROM wea_trade_order WHERE order_status = 2 ORDER BY create_time DESC LIMIT #{limit}) t")
    BigDecimal sumLastHalf(@Param("limit") int limit);
}

