package com.wealth.platform.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.contract.MessageService;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.trade.dto.TradeOrderDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.mapper.TradeOrderMapper;
import com.wealth.platform.trade.vo.TradeOrderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private ObjectProvider<RedisUtil> redisUtilProvider;

    @Captor
    private ArgumentCaptor<WeaTradeOrder> orderCaptor;

    private TradeOrderServiceImpl tradeOrderService;

    private WeaTradeOrder mockOrder;

    @BeforeEach
    void setUp() {
        when(redisUtilProvider.getIfAvailable()).thenReturn(redisUtil);
        tradeOrderService = new TradeOrderServiceImpl(messageService, redisUtilProvider);
        ReflectionTestUtils.setField(tradeOrderService, "baseMapper", tradeOrderMapper);

        mockOrder = new WeaTradeOrder();
        mockOrder.setId(1L);
        mockOrder.setOrderNo("ORDER_TEST123456");
        mockOrder.setUserId(100L);
        mockOrder.setProductCode("P001");
        mockOrder.setTradeType(1);
        mockOrder.setEntrustPrice(new BigDecimal("100.00"));
        mockOrder.setEntrustNum(10);
        mockOrder.setOrderStatus(1);
    }

    @Test
    @DisplayName("创建订单成功-含分布式事务和消息推送")
    void createOrder_Success() {
        TradeOrderDTO dto = new TradeOrderDTO();
        dto.setUserId(100L);
        dto.setProductCode("P001");
        dto.setTradeType(1);
        dto.setEntrustPrice(new BigDecimal("100.00"));
        dto.setEntrustNum(10);

        doReturn(1).when(tradeOrderMapper).insert(any(WeaTradeOrder.class));
        doNothing().when(messageService).createMessage(any(MessageFeignDTO.class));

        boolean result = tradeOrderService.createOrder(dto);

        assertTrue(result);

        // 验证订单保存 - 检查 orderNo/orderStatus 自动填充
        verify(tradeOrderMapper).insert(orderCaptor.capture());
        WeaTradeOrder savedOrder = orderCaptor.getValue();
        assertNotNull(savedOrder.getOrderNo());
        assertTrue(savedOrder.getOrderNo().startsWith("ORDER_"));
        assertEquals(1, savedOrder.getOrderStatus());
        assertEquals(100L, savedOrder.getUserId());

        // 验证消息推送
        verify(messageService).createMessage(argThat(msg ->
                100L == msg.getUserId() &&
                2 == msg.getMsgType() &&
                "trade order submitted".equals(msg.getMsgTitle()) &&
                msg.getMsgContent().contains("P001")
        ));
    }

    @Test
    @DisplayName("创建订单-保存失败抛异常")
    void createOrder_SaveFailed() {
        TradeOrderDTO dto = new TradeOrderDTO();
        dto.setUserId(100L);
        dto.setProductCode("P001");
        dto.setTradeType(1);
        dto.setEntrustPrice(new BigDecimal("100.00"));
        dto.setEntrustNum(10);
        dto.setTradeType(1);

        doReturn(0).when(tradeOrderMapper).insert(any(WeaTradeOrder.class));

        assertThrows(RuntimeException.class, () -> tradeOrderService.createOrder(dto));
        verify(messageService, never()).createMessage(any());
    }

    @Test
    @DisplayName("根据ID查询订单-成功")
    void getOrderById_Found() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(mockOrder);

        TradeOrderVO result = tradeOrderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("ORDER_TEST123456", result.getOrderNo());
        assertEquals(100L, result.getUserId());
    }

    @Test
    @DisplayName("根据ID查询订单-不存在返回null")
    void getOrderById_NotFound() {
        when(tradeOrderMapper.selectById(99L)).thenReturn(null);

        TradeOrderVO result = tradeOrderService.getOrderById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询订单列表-有数据")
    void getOrderList_WithData() {
        Page<WeaTradeOrder> mockPage = new Page<>(1, 1000, 1);
        mockPage.setRecords(List.of(mockOrder));
        when(tradeOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<TradeOrderVO> result = tradeOrderService.getOrderList(1, 10);

        assertEquals(1, result.size());
        assertEquals("ORDER_TEST123456", result.get(0).getOrderNo());
    }

    @Test
    @DisplayName("查询订单列表-空数据")
    void getOrderList_Empty() {
        Page<WeaTradeOrder> mockPage = new Page<>(1, 1000, 0);
        mockPage.setRecords(List.of());
        when(tradeOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<TradeOrderVO> result = tradeOrderService.getOrderList(1, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("分页查询订单-按用户和状态筛选")
    void pageOrders_WithFilters() {
        Page<WeaTradeOrder> page = new Page<>(1, 10);
        Page<WeaTradeOrder> mockPage = new Page<>(1, 10, 1);
        mockPage.setRecords(List.of(mockOrder));

        when(tradeOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<TradeOrderVO> result = tradeOrderService.pageOrders(page, 100L, null, null, 1);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("ORDER_TEST123456", result.getRecords().get(0).getOrderNo());
    }

    @Test
    @DisplayName("分页查询订单-无筛选条件")
    void pageOrders_WithoutFilters() {
        Page<WeaTradeOrder> page = new Page<>(1, 10);
        Page<WeaTradeOrder> mockPage = new Page<>(1, 10, 3);
        mockPage.setRecords(List.of(mockOrder));

        when(tradeOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<TradeOrderVO> result = tradeOrderService.pageOrders(page, null, null, null, null);

        assertEquals(3, result.getTotal());
    }

    @Test
    @DisplayName("更新订单成功-null安全更新")
    void updateOrder_Success() {
        TradeOrderDTO dto = new TradeOrderDTO();
        dto.setEntrustPrice(new BigDecimal("150.00"));

        when(tradeOrderMapper.selectById(1L)).thenReturn(mockOrder);
        when(tradeOrderMapper.updateById(any(WeaTradeOrder.class))).thenReturn(1);

        boolean result = tradeOrderService.updateOrder(1L, dto);

        assertTrue(result);
        verify(tradeOrderMapper).updateById(argThat((WeaTradeOrder order) ->
                order.getId() == 1L &&
                new BigDecimal("150.00").compareTo(order.getEntrustPrice()) == 0
        ));
    }

    @Test
    @DisplayName("更新订单-不存在抛异常")
    void updateOrder_NotFound() {
        when(tradeOrderMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> tradeOrderService.updateOrder(99L, new TradeOrderDTO()));
        verify(tradeOrderMapper, never()).updateById(isA(WeaTradeOrder.class));
    }

    @Test
    @DisplayName("删除订单成功")
    void deleteOrder_Success() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(mockOrder);
        when(tradeOrderMapper.deleteById(1L)).thenReturn(1);

        boolean result = tradeOrderService.deleteOrder(1L);

        assertTrue(result);
        verify(tradeOrderMapper).deleteById(1L);
    }
}
