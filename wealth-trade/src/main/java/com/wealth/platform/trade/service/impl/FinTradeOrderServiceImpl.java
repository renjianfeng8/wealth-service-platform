package com.wealth.platform.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.feign.MessageFeignClient;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.trade.dto.FinTradeOrderDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.mapper.FinTradeOrderMapper;
import com.wealth.platform.trade.service.FinTradeOrderService;
import com.wealth.platform.trade.vo.FinTradeOrderVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinTradeOrderServiceImpl extends ServiceImpl<FinTradeOrderMapper, WeaTradeOrder>
        implements FinTradeOrderService {

    private final MessageFeignClient messageFeignClient;

    public FinTradeOrderServiceImpl(MessageFeignClient messageFeignClient) {
        this.messageFeignClient = messageFeignClient;
    }

    @Override
    public FinTradeOrderVO getOrderById(Long id) {
        WeaTradeOrder order = getById(id);
        if (order == null) {
            return null;
        }
        FinTradeOrderVO vo = new FinTradeOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public List<FinTradeOrderVO> getOrderList() {
        List<WeaTradeOrder> list = list();
        return list.stream().map(order -> {
            FinTradeOrderVO vo = new FinTradeOrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(name = "trade-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(FinTradeOrderDTO dto) {
        WeaTradeOrder order = new WeaTradeOrder();
        BeanUtils.copyProperties(dto, order);

        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        order.setOrderNo(orderNo);

        order.setOrderStatus(1);

        boolean saved = save(order);
        if (!saved) {
            throw new RuntimeException("order save failed");
        }

        MessageFeignDTO msg = new MessageFeignDTO();
        msg.setUserId(dto.getUserId());
        msg.setMsgType(2);
        msg.setMsgTitle("trade order submitted");
        msg.setMsgContent("order " + orderNo + " submitted, product: " + dto.getProductCode());
        messageFeignClient.createMessage(msg);

        return true;
    }

    @Override
    public IPage<FinTradeOrderVO> pageOrders(Page<WeaTradeOrder> page, Long userId, Integer orderStatus) {
        LambdaQueryWrapper<WeaTradeOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WeaTradeOrder::getUserId, userId);
        }
        if (orderStatus != null) {
            wrapper.eq(WeaTradeOrder::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(WeaTradeOrder::getCreateTime);

        IPage<WeaTradeOrder> entityPage = page(page, wrapper);
        Page<FinTradeOrderVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinTradeOrderVO.class));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(Long id, FinTradeOrderDTO dto) {
        WeaTradeOrder order = getById(id);
        if (order == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, order);
        order.setId(id);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }
}