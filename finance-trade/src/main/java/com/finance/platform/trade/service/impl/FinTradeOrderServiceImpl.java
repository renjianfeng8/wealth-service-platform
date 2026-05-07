package com.finance.platform.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.trade.dto.FinTradeOrderDTO;
import com.finance.platform.trade.entity.FinTradeOrder;
import com.finance.platform.trade.mapper.FinTradeOrderMapper;
import com.finance.platform.trade.service.FinTradeOrderService;
import com.finance.platform.trade.vo.FinTradeOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinTradeOrderServiceImpl extends ServiceImpl<FinTradeOrderMapper, FinTradeOrder>
        implements FinTradeOrderService {

    @Override
    public FinTradeOrderVO getOrderById(Long id) {
        FinTradeOrder order = getById(id);
        if (order == null) {
            return null;
        }
        FinTradeOrderVO vo = new FinTradeOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public List<FinTradeOrderVO> getOrderList() {
        List<FinTradeOrder> list = list();
        return list.stream().map(order -> {
            FinTradeOrderVO vo = new FinTradeOrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(FinTradeOrderDTO dto) {
        FinTradeOrder order = new FinTradeOrder();
        BeanUtils.copyProperties(dto, order);

        // 自动生成订单号
        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        order.setOrderNo(orderNo);

        // 默认待委托状态
        order.setOrderStatus(1);

        return save(order);
    }

    @Override
    public boolean updateOrder(Long id, FinTradeOrderDTO dto) {
        FinTradeOrder order = getById(id);
        if (order == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, order);
        order.setId(id);
        return updateById(order);
    }

    @Override
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }
}