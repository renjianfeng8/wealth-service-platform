package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.MessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.mapper.MessageMapper;
import com.wealth.platform.message.service.MessageService;
import com.wealth.platform.message.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl extends BaseBizServiceImpl<MessageMapper, WeaMessage>
        implements MessageService, com.wealth.common.contract.MessageService {

    @Override
    public MessageVO getMessageById(Long id) {
        return getVoByIdOrThrow(id, MessageVO.class, "消息");
    }

    @Override
    public List<MessageVO> getMessageList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, MessageVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMessage(MessageDTO dto) {
        WeaMessage entity = BeanConvertUtil.convert(dto, WeaMessage.class);
        entity.setReadFlag(0);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(Long id, MessageDTO dto) {
        return updateDto(id, dto, "消息");
    }

    @Override
    public IPage<MessageVO> pageMessages(Page<WeaMessage> page, Long userId, String msgTitle, Integer msgType, Integer readFlag) {
        return pageVoWithFilter(page, MessageVO.class, orderByDesc(WeaMessage::getCreateTime),
                eq(WeaMessage::getUserId, userId),
                like(WeaMessage::getMsgTitle, msgTitle),
                eq(WeaMessage::getMsgType, msgType),
                eq(WeaMessage::getReadFlag, readFlag));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id) {
        WeaMessage entity = getEntityOrThrow(id, "消息");
        entity.setReadFlag(1);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids) {
        List<WeaMessage> list = listByIds(ids);
        for (WeaMessage msg : list) {
            msg.setReadFlag(1);
        }
        updateBatchById(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long id) {
        return deleteWithCheck(id, "消息");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMessage(MessageFeignDTO dto) {
        MessageDTO msgDto = new MessageDTO();
        msgDto.setUserId(dto.getUserId());
        msgDto.setMsgType(dto.getMsgType());
        msgDto.setMsgTitle(dto.getMsgTitle());
        msgDto.setMsgContent(dto.getMsgContent());
        createMessage(msgDto);
    }
}
