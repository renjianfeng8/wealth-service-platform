package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.message.dto.FinMessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.mapper.FinMessageMapper;
import com.wealth.platform.message.vo.FinMessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinMessageServiceImplTest {

    @Mock
    private FinMessageMapper messageMapper;

    private FinMessageServiceImpl messageService;

    private WeaMessage mockMessage;

    @BeforeEach
    void setUp() {
        messageService = new FinMessageServiceImpl();
        ReflectionTestUtils.setField(messageService, "baseMapper", messageMapper);

        mockMessage = new WeaMessage();
        mockMessage.setId(1L);
        mockMessage.setUserId(100L);
        mockMessage.setMsgType(1);
        mockMessage.setMsgTitle("测试消息标题");
        mockMessage.setMsgContent("测试消息内容");
        mockMessage.setReadFlag(0);
    }

    @Test
    @DisplayName("创建消息成功")
    void createMessage_Success() {
        FinMessageDTO dto = new FinMessageDTO();
        dto.setUserId(100L);
        dto.setMsgType(1);
        dto.setMsgTitle("新消息");
        dto.setMsgContent("新消息内容");

        doReturn(1).when(messageMapper).insert(any(WeaMessage.class));

        boolean result = messageService.createMessage(dto);

        assertTrue(result);
        verify(messageMapper).insert(argThat((WeaMessage msg) ->
                "新消息".equals(msg.getMsgTitle()) &&
                "新消息内容".equals(msg.getMsgContent()) &&
                0 == msg.getReadFlag()
        ));
    }

    @Test
    @DisplayName("根据ID查询消息-成功")
    void getMessageById_Found() {
        when(messageMapper.selectById(1L)).thenReturn(mockMessage);

        FinMessageVO result = messageService.getMessageById(1L);

        assertNotNull(result);
        assertEquals("测试消息标题", result.getMsgTitle());
        assertEquals(0, result.getReadFlag());
    }

    @Test
    @DisplayName("根据ID查询消息-不存在返回null")
    void getMessageById_NotFound() {
        when(messageMapper.selectById(99L)).thenReturn(null);

        FinMessageVO result = messageService.getMessageById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询消息列表-有数据")
    void getMessageList_WithData() {
        when(messageMapper.selectList(any())).thenReturn(List.of(mockMessage));

        List<FinMessageVO> result = messageService.getMessageList();

        assertEquals(1, result.size());
        assertEquals("测试消息标题", result.get(0).getMsgTitle());
    }

    @Test
    @DisplayName("分页查询消息-按用户筛选")
    void pageMessages_WithUserId() {
        Page<WeaMessage> page = new Page<>(1, 10);
        Page<WeaMessage> mockPage = new Page<>(1, 10, 1);
        mockPage.setRecords(List.of(mockMessage));

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<FinMessageVO> result = messageService.pageMessages(page, 100L);

        assertEquals(1, result.getTotal());
        assertEquals("测试消息标题", result.getRecords().get(0).getMsgTitle());
    }

    @Test
    @DisplayName("分页查询消息-无用户筛选")
    void pageMessages_WithoutUserId() {
        Page<WeaMessage> page = new Page<>(1, 10);
        Page<WeaMessage> mockPage = new Page<>(1, 10, 3);
        mockPage.setRecords(List.of(mockMessage));

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<FinMessageVO> result = messageService.pageMessages(page, null);

        assertEquals(3, result.getTotal());
    }

    @Test
    @DisplayName("更新消息成功-null安全更新")
    void updateMessage_Success() {
        FinMessageDTO dto = new FinMessageDTO();
        dto.setMsgTitle("更新后的标题");

        when(messageMapper.selectById(1L)).thenReturn(mockMessage);
        when(messageMapper.updateById(any(WeaMessage.class))).thenReturn(1);

        boolean result = messageService.updateMessage(1L, dto);

        assertTrue(result);
        verify(messageMapper).updateById(argThat((WeaMessage msg) ->
                msg.getId() == 1L && "更新后的标题".equals(msg.getMsgTitle())
        ));
    }

    @Test
    @DisplayName("更新消息-不存在返回false")
    void updateMessage_NotFound() {
        when(messageMapper.selectById(99L)).thenReturn(null);

        boolean result = messageService.updateMessage(99L, new FinMessageDTO());

        assertFalse(result);
        verify(messageMapper, never()).updateById(isA(WeaMessage.class));
    }

    @Test
    @DisplayName("删除消息成功")
    void deleteMessage_Success() {
        when(messageMapper.deleteById(1L)).thenReturn(1);

        boolean result = messageService.deleteMessage(1L);

        assertTrue(result);
        verify(messageMapper).deleteById(1L);
    }
}
