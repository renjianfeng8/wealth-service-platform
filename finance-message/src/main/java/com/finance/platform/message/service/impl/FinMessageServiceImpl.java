package com.finance.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.message.dto.FinMessageDTO;
import com.finance.platform.message.entity.FinMessage;
import com.finance.platform.message.mapper.FinMessageMapper;
import com.finance.platform.message.service.FinMessageService;
import com.finance.platform.message.vo.FinMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinMessageServiceImpl extends ServiceImpl<FinMessageMapper, FinMessage> implements FinMessageService {

    @Override
    public FinMessageVO getMessageById(Long id) {
        FinMessage entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinMessageVO vo = new FinMessageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinMessageVO> getMessageList() {
        List<FinMessage> list = list();
        return list.stream().map(entity -> {
            FinMessageVO vo = new FinMessageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMessage(FinMessageDTO dto) {
        FinMessage entity = new FinMessage();
        BeanUtils.copyProperties(dto, entity);
        entity.setReadFlag(0);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(Long id, FinMessageDTO dto) {
        FinMessage entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    public IPage<FinMessageVO> pageMessages(Page<FinMessage> page, Long userId) {
        LambdaQueryWrapper<FinMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FinMessage::getUserId, userId);
        }
        wrapper.orderByDesc(FinMessage::getCreateTime);

        IPage<FinMessage> entityPage = page(page, wrapper);
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