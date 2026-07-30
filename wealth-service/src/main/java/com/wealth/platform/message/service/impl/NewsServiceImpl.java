package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.LikeUtil;
import com.wealth.platform.message.dto.NewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.mapper.NewsMapper;
import com.wealth.platform.message.service.NewsService;
import com.wealth.platform.message.vo.NewsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class NewsServiceImpl extends BaseBizServiceImpl<NewsMapper, WeaNews>
        implements NewsService {

    @Override
    public NewsVO getNewsById(Long id) {
        return getVoById(id, NewsVO.class);
    }

    @Override
    public List<NewsVO> getNewsList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, NewsVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNews(NewsDTO dto) {
        long count = lambdaQuery().eq(WeaNews::getTitle, dto.getTitle()).count();
        if (count > 0) {
            throw new ServiceException(400, "资讯标题已存在");
        }
        WeaNews entity = BeanConvertUtil.convert(dto, WeaNews.class);
        return save(entity);
    }

    @Override
    public IPage<NewsVO> pageNews(Page<WeaNews> page, String title, String source, Integer newsType) {
        LambdaQueryWrapper<WeaNews> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(WeaNews::getTitle, LikeUtil.escape(title));
        }
        if (StringUtils.hasText(source)) {
            wrapper.like(WeaNews::getSource, LikeUtil.escape(source));
        }
        if (newsType != null) {
            wrapper.eq(WeaNews::getNewsType, newsType);
        }
        wrapper.orderByDesc(WeaNews::getPublishTime);
        return BeanConvertUtil.convertPage(baseMapper.selectPage(page, wrapper), NewsVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNews(Long id, NewsDTO dto) {
        return updateDto(id, dto, "资讯");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNews(Long id) {
        return deleteWithCheck(id, "资讯");
    }
}
