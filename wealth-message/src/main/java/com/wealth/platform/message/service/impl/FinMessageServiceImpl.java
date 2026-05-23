package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.FinMessageDTO;
import com.wealth.platform.message.entity.WeaMessage;
import com.wealth.platform.message.mapper.FinMessageMapper;
import com.wealth.platform.message.service.FinMessageService;
import com.wealth.platform.message.vo.FinMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinMessageServiceImpl extends ServiceImpl<FinMessageMapper, WeaMessage> implements FinMessageService {

    @Override
    public FinMessageVO getMessageById(Long id) {
        WeaMessage entity = getById(id);
        if (entity == null) {
            return null;
        }
        return BeanConvertUtil.convert(entity, FinMessageVO.class);
    }

    @Override
    public List<FinMessageVO> getMessageList() {
        List<WeaMessage> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> BeanConvertUtil.convert(entity, FinMessageVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMessage(FinMessageDTO dto) {
        WeaMessage entity = BeanConvertUtil.convert(dto, WeaMessage.class);
        entity.setReadFlag(0);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(Long id, FinMessageDTO dto) {
        WeaMessage entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "消息不存在");
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
