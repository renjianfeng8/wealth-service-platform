package com.wealth.platform.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.contract.DashboardTradeOrderProvider;
import com.wealth.common.contract.MessageService;
import com.wealth.common.dto.DashboardTradeOrderDTO;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.common.utils.LikeUtil;
import com.wealth.platform.trade.constant.OrderStatusEnum;
import com.wealth.platform.trade.dto.TradeOrderDTO;
import com.wealth.platform.trade.dto.TradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.mapper.TradeOrderMapper;
import com.wealth.platform.trade.service.TradeOrderService;
import com.wealth.platform.trade.vo.TradeOrderVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeOrderServiceImpl extends BaseBizServiceImpl<TradeOrderMapper, WeaTradeOrder>
        implements TradeOrderService, DashboardTradeOrderProvider {

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:trade:";
    private static final long IDEMPOTENT_TTL_HOURS = 24;

    private final MessageService messageService;
    private final RedisUtil redisUtil;

    public TradeOrderServiceImpl(MessageService messageService, ObjectProvider<RedisUtil> redisUtilProvider) {
        this.messageService = messageService;
        this.redisUtil = redisUtilProvider.getIfAvailable();
    }

    @Override
    public TradeOrderVO getOrderById(Long id) {
        return getVoById(id, TradeOrderVO.class);
    }

    @Override
    public List<TradeOrderVO> getOrderList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, TradeOrderVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(TradeOrderDTO dto) {
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
        if (dto.getIdempotentKey() != null && redisUtil != null) {
            redisUtil.safeExecuteVoid(() -> redisUtil.set(IDEMPOTENT_KEY_PREFIX + dto.getIdempotentKey(), orderNo, IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS), "幂等键未记录");
        }

        // 6. 发送通知消息（Feign 调用）
        MessageFeignDTO msg = new MessageFeignDTO();
        msg.setUserId(dto.getUserId());
        msg.setMsgType(2);
        msg.setMsgTitle("trade order submitted");
        msg.setMsgContent("order " + orderNo + " submitted, product: " + dto.getProductCode());
        messageService.createMessage(msg);

        return true;
    }

    @Override
    public IPage<TradeOrderVO> pageOrders(Page<WeaTradeOrder> page, Long userId, String orderNo, String productCode, Integer orderStatus) {
        LambdaQueryWrapper<WeaTradeOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WeaTradeOrder::getUserId, userId);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(WeaTradeOrder::getOrderNo, LikeUtil.escape(orderNo));
        }
        if (StringUtils.hasText(productCode)) {
            wrapper.like(WeaTradeOrder::getProductCode, LikeUtil.escape(productCode));
        }
        if (orderStatus != null) {
            wrapper.eq(WeaTradeOrder::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(WeaTradeOrder::getCreateTime);
        return BeanConvertUtil.convertPage(baseMapper.selectPage(page, wrapper), TradeOrderVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(Long id, TradeOrderDTO dto) {
        return updateDto(id, dto, "订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long id, TradeOrderStatusDTO dto) {
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
        return deleteWithCheck(id, "订单");
    }

    /**
     * 幂等性校验：客户端传入 idempotentKey 时，检查 Redis 中是否已存在。
     * Redis 不可用时跳过校验（不阻塞订单提交），仅记录警告日志。
     */
    private void checkIdempotent(TradeOrderDTO dto) {
        String key = dto.getIdempotentKey();
        if (key == null || key.isBlank()) {
            return; // 未传幂等键，跳过校验（兼容旧客户端）
        }
        if (redisUtil == null) {
            log.warn("RedisUtil 不可用，跳过幂等性校验");
            return;
        }

        String redisKey = IDEMPOTENT_KEY_PREFIX + key;
        Boolean exists = redisUtil.safeExecute(() -> redisUtil.hasKey(redisKey), false, "跳过幂等性校验");
        if (Boolean.TRUE.equals(exists)) {
            Object existing = redisUtil.get(redisKey);
            throw new ServiceException(400, "请勿重复提交，已有订单: " + existing);
        }
    }

    @Override
    public BigDecimal sumCompletedAmount() {
        return baseMapper.sumCompletedAmount();
    }

    @Override
    public long countCompletedOrders() {
        return lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, OrderStatusEnum.MATCHED.getCode())
                .count();
    }

    @Override
    public BigDecimal sumFirstHalf(int limit) {
        return baseMapper.sumFirstHalf(limit);
    }

    @Override
    public BigDecimal sumLastHalf(int limit) {
        return baseMapper.sumLastHalf(limit);
    }

    @Override
    public BigDecimal sumTodayCompletedAmount() {
        return baseMapper.sumTodayCompletedAmount();
    }

    @Override
    public List<DashboardTradeOrderDTO> findCompletedOrders(LocalDateTime startTime, LocalDateTime endTime) {
        List<WeaTradeOrder> orders = lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, OrderStatusEnum.MATCHED.getCode())
                .ge(WeaTradeOrder::getCreateTime, startTime)
                .le(WeaTradeOrder::getCreateTime, endTime)
                .last("LIMIT 1000")
                .list();
        return BeanConvertUtil.convertList(orders, DashboardTradeOrderDTO.class);
    }
}
