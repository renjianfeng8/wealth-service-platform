package com.wealth.platform.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.trade.dto.FinTradeOrderDTO;
import com.wealth.platform.trade.dto.FinTradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.vo.FinTradeOrderVO;
import java.util.List;

/**
 * 交易委托单业务层接口。
 */
public interface FinTradeOrderService extends IService<WeaTradeOrder> {

    FinTradeOrderVO getOrderById(Long id);

    List<FinTradeOrderVO> getOrderList();

    IPage<FinTradeOrderVO> pageOrders(Page<WeaTradeOrder> page, Long userId, Integer orderStatus);

    boolean createOrder(FinTradeOrderDTO dto);

    boolean updateOrder(Long id, FinTradeOrderDTO dto);

    boolean deleteOrder(Long id);

    /**
     * 更新订单状态（含状态机校验）
     */
    boolean updateOrderStatus(Long id, FinTradeOrderStatusDTO dto);
}