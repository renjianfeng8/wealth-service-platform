package com.wealth.platform.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.trade.dto.FinTradeOrderDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.vo.FinTradeOrderVO;
import java.util.List;

/**
 * 浜ゆ槗濮旀墭鍗曚笟鍔″眰鎺ュ彛銆?
 */
public interface FinTradeOrderService extends IService<WeaTradeOrder> {

    /**
     * 鏍规嵁ID鏌ヨ璁㈠崟
     */
    FinTradeOrderVO getOrderById(Long id);

    /**
     * 鏌ヨ璁㈠崟鍒楄〃
     */
    List<FinTradeOrderVO> getOrderList();

    /**
     * 鍒嗛〉鏌ヨ璁㈠崟锛屾敮鎸佹寜鐢ㄦ埛鍜岀姸鎬佺瓫閫?
     */
    IPage<FinTradeOrderVO> pageOrders(Page<WeaTradeOrder> page, Long userId, Integer orderStatus);

    /**
     * 鍒涘缓璁㈠崟
     */
    boolean createOrder(FinTradeOrderDTO dto);

    /**
     * 鏇存柊璁㈠崟
     */
    boolean updateOrder(Long id, FinTradeOrderDTO dto);

    /**
     * 鍒犻櫎璁㈠崟
     */
    boolean deleteOrder(Long id);
}