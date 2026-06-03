package com.wealth.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.message.dto.NewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.vo.NewsVO;
import java.util.List;

/**
 * 财经资讯公告业务层接口
 */
public interface NewsService extends IService<WeaNews> {

    NewsVO getNewsById(Long id);

    List<NewsVO> getNewsList();

    IPage<NewsVO> pageNews(Page<WeaNews> page, String title, String source, Integer newsType);

    boolean createNews(NewsDTO dto);

    boolean updateNews(Long id, NewsDTO dto);

    boolean deleteNews(Long id);
}