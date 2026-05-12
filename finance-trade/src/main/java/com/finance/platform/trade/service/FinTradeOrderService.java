package com.finance.platform.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.trade.dto.FinTradeOrderDTO;
import com.finance.platform.trade.entity.FinTradeOrder;
import com.finance.platform.trade.vo.FinTradeOrderVO;
import java.util.List;

/**
 * 交易委托单业务层接口。
 */
public interface FinTradeOrderService extends IService<FinTradeOrder> {

    /**
     * 根据ID查询订单
     */
    FinTradeOrderVO getOrderById(Long id);

    /**
     * 查询订单列表
     */
    List<FinTradeOrderVO> getOrderList();

    /**
     * 分页查询订单，支持按用户和状态筛选
     */
    IPage<FinTradeOrderVO> pageOrders(Page<FinTradeOrder> page, Long userId, Integer orderStatus);

    /**
     * 创建订单
     */
    boolean createOrder(FinTradeOrderDTO dto);

    /**
     * 更新订单
     */
    boolean updateOrder(Long id, FinTradeOrderDTO dto);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);
}