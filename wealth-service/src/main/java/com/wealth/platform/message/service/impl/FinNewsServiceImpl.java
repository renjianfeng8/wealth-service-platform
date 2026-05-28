package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.FinNewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.mapper.FinNewsMapper;
import com.wealth.platform.message.service.FinNewsService;
import com.wealth.platform.message.vo.FinNewsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinNewsServiceImpl extends ServiceImpl<FinNewsMapper, WeaNews>
        implements FinNewsService {

    @Override
    public FinNewsVO getNewsById(Long id) {
        WeaNews entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinNewsVO vo = BeanConvertUtil.convert(entity, FinNewsVO.class);
        return vo;
    }

    @Override
    public List<FinNewsVO> getNewsList() {
        List<WeaNews> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> {
            FinNewsVO vo = BeanConvertUtil.convert(entity, FinNewsVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNews(FinNewsDTO dto) {
        long count = lambdaQuery().eq(WeaNews::getTitle, dto.getTitle()).count();
        if (count > 0) {
            throw new ServiceException(400, "资讯标题已存在");
        }
        WeaNews entity = BeanConvertUtil.convert(dto, WeaNews.class);
        return save(entity);
    }

    @Override
    public IPage<FinNewsVO> pageNews(Page<WeaNews> page, String title, String source, Integer newsType) {
        LambdaQueryWrapper<WeaNews> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(WeaNews::getTitle, title);
        }
        if (StringUtils.hasText(source)) {
            wrapper.like(WeaNews::getSource, source);
        }
        if (newsType != null) {
            wrapper.eq(WeaNews::getNewsType, newsType);
        }
        wrapper.orderByDesc(WeaNews::getPublishTime);
        return BeanConvertUtil.convertPage(baseMapper.selectPage(page, wrapper), FinNewsVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNews(Long id, FinNewsDTO dto) {
        WeaNews entity = getById(id);
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