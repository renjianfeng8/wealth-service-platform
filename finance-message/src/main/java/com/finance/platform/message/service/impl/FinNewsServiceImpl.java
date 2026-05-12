package com.finance.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.message.dto.FinNewsDTO;
import com.finance.platform.message.entity.FinNews;
import com.finance.platform.message.mapper.FinNewsMapper;
import com.finance.platform.message.service.FinNewsService;
import com.finance.platform.message.vo.FinNewsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinNewsServiceImpl extends ServiceImpl<FinNewsMapper, FinNews>
        implements FinNewsService {

    @Override
    public FinNewsVO getNewsById(Long id) {
        FinNews entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinNewsVO vo = new FinNewsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinNewsVO> getNewsList() {
        List<FinNews> list = list();
        return list.stream().map(entity -> {
            FinNewsVO vo = new FinNewsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNews(FinNewsDTO dto) {
        FinNews entity = new FinNews();
        BeanUtils.copyProperties(dto, entity);
        return save(entity);
    }

    @Override
    public IPage<FinNewsVO> pageNews(Page<FinNews> page, Integer newsType) {
        LambdaQueryWrapper<FinNews> wrapper = new LambdaQueryWrapper<>();
        if (newsType != null && newsType > 0) {
            wrapper.eq(FinNews::getNewsType, newsType);
        }
        wrapper.orderByDesc(FinNews::getCreateTime);

        IPage<FinNews> entityPage = page(page, wrapper);
        Page<FinNewsVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinNewsVO.class));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNews(Long id, FinNewsDTO dto) {
        FinNews entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNews(Long id) {
        return removeById(id);
    }
}