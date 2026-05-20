package com.wealth.platform.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.feign.MessageFeignClient;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.trade.constant.OrderStatusEnum;
import com.wealth.platform.trade.dto.FinTradeOrderDTO;
import com.wealth.platform.trade.dto.FinTradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.mapper.FinTradeOrderMapper;
import com.wealth.platform.trade.service.FinTradeOrderService;
import com.wealth.platform.trade.vo.FinTradeOrderVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FinTradeOrderServiceImpl extends ServiceImpl<FinTradeOrderMapper, WeaTradeOrder>
        implements FinTradeOrderService {

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:trade:";
    private static final long IDEMPOTENT_TTL_HOURS = 24;

    private final MessageFeignClient messageFeignClient;
    private final RedisUtil redisUtil;

    public FinTradeOrderServiceImpl(MessageFeignClient messageFeignClient, RedisUtil redisUtil) {
        this.messageFeignClient = messageFeignClient;
        this.redisUtil = redisUtil;
    }

    @Override
    public FinTradeOrderVO getOrderById(Long id) {
        WeaTradeOrder order = getById(id);
        if (order == null) {
            return null;
        }
        FinTradeOrderVO vo = BeanConvertUtil.convert(order, FinTradeOrderVO.class);
        return vo;
    }

    @Override
    public List<FinTradeOrderVO> getOrderList() {
        List<WeaTradeOrder> list = list();
        return list.stream().map(order -> {
            FinTradeOrderVO vo = BeanConvertUtil.convert(order, FinTradeOrderVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(name = "trade-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(FinTradeOrderDTO dto) {
        // 1. 幂等性校验
        checkIdempotent(dto);

        // 2. 校验交易类型
        if (dto.getTradeType() != 1 && dto.getTradeType() != 2) {
            throw new ServiceException(400, "无效的交易类型，仅支持 1=买入 2=卖出");
        }

        // 3. DTO → Entity
        WeaTradeOrder order = BeanConvertUtil.convert(dto, WeaTradeOrder.class);

        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        order.setOrderNo(orderNo);
        order.setOrderStatus(OrderStatusEnum.SUBMITTED.getCode());

        // 4. 保存订单
        boolean saved = save(order);
        if (!saved) {
            throw new ServiceException(500, "订单保存失败");
        }

        // 5. 记录幂等键（设置 TTL 防止 Redis 内存泄漏）
        if (dto.getIdempotentKey() != null) {
            redisUtil.set(IDEMPOTENT_KEY_PREFIX + dto.getIdempotentKey(), orderNo, IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS);
        }

        // 6. 发送通知消息（Feign 调用）
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
            if (!OrderStatusEnum.isValidStatus(orderStatus)) {
                throw new ServiceException(400, "无效的订单状态");
            }
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
            throw new ServiceException(404, "订单不存在");
        }
        BeanConvertUtil.copyNonNullProperties(dto, order);
        order.setId(id);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long id, FinTradeOrderStatusDTO dto) {
        WeaTradeOrder order = getById(id);
        if (order == null) {
            throw new ServiceException(404, "订单不存在");
        }

        int from = order.getOrderStatus();
        int to = dto.getOrderStatus();

        if (!OrderStatusEnum.isValidStatus(to)) {
            throw new ServiceException(400, "无效的订单状态");
        }
        if (!OrderStatusEnum.isValidTransition(from, to)) {
            throw new ServiceException(400, "非法状态转换: " + from + " → " + to);
        }

        order.setOrderStatus(to);
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }

    /**
     * 幂等性校验：客户端传入 idempotentKey 时，检查 Redis 中是否已存在。
     */
    private void checkIdempotent(FinTradeOrderDTO dto) {
        String key = dto.getIdempotentKey();
        if (key == null || key.isBlank()) {
            return; // 未传幂等键，跳过校验（兼容旧客户端）
        }

        String redisKey = IDEMPOTENT_KEY_PREFIX + key;
        if (Boolean.TRUE.equals(redisUtil.hasKey(redisKey))) {
            Object existing = redisUtil.get(redisKey);
            throw new ServiceException(400, "请勿重复提交，已有订单: " + existing);
        }
    }
}
