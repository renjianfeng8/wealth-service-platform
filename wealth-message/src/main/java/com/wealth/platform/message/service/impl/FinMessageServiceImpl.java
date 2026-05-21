package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.FinMessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.mapper.FinMessageMapper;
import com.wealth.platform.message.mq.RabbitMqProducer;
import com.wealth.platform.message.service.FinMessageService;
import com.wealth.platform.message.vo.FinMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.wealth.platform.message.config.RabbitMqConfig.EXCHANGE_MSG;
import static com.wealth.platform.message.config.RabbitMqConfig.ROUTING_KEY_MSG;

@Slf4j
@Service
public class FinMessageServiceImpl extends ServiceImpl<FinMessageMapper, WeaMessage> implements FinMessageService {

    private final RabbitMqProducer rabbitMqProducer;
    private final ObjectMapper objectMapper;

    public FinMessageServiceImpl(RabbitMqProducer rabbitMqProducer, ObjectMapper objectMapper) {
        this.rabbitMqProducer = rabbitMqProducer;
        this.objectMapper = objectMapper;
    }

    @Override
    public FinMessageVO getMessageById(Long id) {
        WeaMessage entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinMessageVO vo = BeanConvertUtil.convert(entity, FinMessageVO.class);
        return vo;
    }

    @Override
    public List<FinMessageVO> getMessageList() {
        List<WeaMessage> list = list(new LambdaQueryWrapper<WeaMessage>().last("LIMIT 1000"));
        return list.stream().map(entity -> {
            FinMessageVO vo = BeanConvertUtil.convert(entity, FinMessageVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMessage(FinMessageDTO dto) {
        WeaMessage entity = BeanConvertUtil.convert(dto, WeaMessage.class);
        entity.setReadFlag(0);
        boolean saved = save(entity);

        // 异步推送消息到 RabbitMQ，失败不影响主流程
        if (saved) {
            try {
                String json = objectMapper.writeValueAsString(entity);
                rabbitMqProducer.send(EXCHANGE_MSG, ROUTING_KEY_MSG, json);
            } catch (JsonProcessingException e) {
                log.warn("消息序列化失败，RabbitMQ 推送跳过", e);
            }
        }

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(Long id, FinMessageDTO dto) {
        WeaMessage entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    public IPage<FinMessageVO> pageMessages(Page<WeaMessage> page, Long userId) {
        LambdaQueryWrapper<WeaMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WeaMessage::getUserId, userId);
        }
        wrapper.orderByDesc(WeaMessage::getCreateTime);

        IPage<WeaMessage> entityPage = page(page, wrapper);
        Page<FinMessageVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinMessageVO.class));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long id) {
        return removeById(id);
    }
}
