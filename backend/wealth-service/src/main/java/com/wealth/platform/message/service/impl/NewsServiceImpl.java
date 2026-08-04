package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.message.dto.NewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.mapper.NewsMapper;
import com.wealth.platform.message.service.NewsService;
import com.wealth.platform.message.vo.NewsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsServiceImpl extends BaseBizServiceImpl<NewsMapper, WeaNews>
        implements NewsService {

    @Override
    public NewsVO getNewsById(Long id) {
        return getVoByIdOrThrow(id, NewsVO.class, "资讯");
    }

    @Override
    public List<NewsVO> getNewsList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, NewsVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNews(NewsDTO dto) {
        checkUnique(WeaNews::getTitle, dto.getTitle(), "资讯标题已存在");
        WeaNews entity = BeanConvertUtil.convert(dto, WeaNews.class);
        return save(entity);
    }

    @Override
    public IPage<NewsVO> pageNews(Page<WeaNews> page, String title, String source, Integer newsType) {
        return pageVoWithFilter(page, NewsVO.class, orderByDesc(WeaNews::getPublishTime),
                like(WeaNews::getTitle, title),
                like(WeaNews::getSource, source),
                eq(WeaNews::getNewsType, newsType));
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
