package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, WeaMessage>
        implements MessageService, com.wealth.common.contract.MessageService {

    @Override
    public MessageVO getMessageById(Long id) {
        WeaMessage entity = getById(id);
        if (entity == null) {
            return null;
        }
        return BeanConvertUtil.convert(entity, MessageVO.class);
    }

    @Override
    public List<MessageVO> getMessageList() {
        List<WeaMessage> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> BeanConvertUtil.convert(entity, MessageVO.class))
                .collect(Collectors.toList());
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
        WeaMessage entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "消息不存在");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    public IPage<MessageVO> pageMessages(Page<WeaMessage> page, Long userId, String msgTitle, Integer msgType, Integer readFlag) {
        LambdaQueryWrapper<WeaMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WeaMessage::getUserId, userId);
        }
        if (StringUtils.hasText(msgTitle)) {
            wrapper.like(WeaMessage::getMsgTitle, msgTitle);
        }
        if (msgType != null) {
            wrapper.eq(WeaMessage::getMsgType, msgType);
        }
        if (readFlag != null) {
            wrapper.eq(WeaMessage::getReadFlag, readFlag);
        }
        wrapper.orderByDesc(WeaMessage::getCreateTime);
        return BeanConvertUtil.convertPage(baseMapper.selectPage(page, wrapper), MessageVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long id) {
        if (getById(id) == null) {
            throw new ServiceException(404, "message not found");
        }
        return removeById(id);
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
