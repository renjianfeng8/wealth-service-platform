package com.wealth.platform.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.trade.dto.TradeOrderDTO;
import com.wealth.platform.trade.dto.TradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.vo.TradeOrderVO;
import java.util.List;

/**
 * 交易委托单业务层接口。
 */
public interface TradeOrderService extends IService<WeaTradeOrder> {

    TradeOrderVO getOrderById(Long id);

    List<TradeOrderVO> getOrderList(Integer pageNum, Integer pageSize);

    IPage<TradeOrderVO> pageOrders(Page<WeaTradeOrder> page, Long userId, String orderNo, String productCode, Integer orderStatus);

    boolean createOrder(TradeOrderDTO dto);

    boolean updateOrder(Long id, TradeOrderDTO dto);

    boolean deleteOrder(Long id);

    /**
     * 更新订单状态（含状态机校验）
     */
    boolean updateOrderStatus(Long id, TradeOrderStatusDTO dto);
}