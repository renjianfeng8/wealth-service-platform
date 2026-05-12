package com.finance.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.message.dto.FinNewsDTO;
import com.finance.platform.message.entity.FinNews;
import com.finance.platform.message.vo.FinNewsVO;
import java.util.List;

/**
 * 财经资讯公告业务层接口。
 */
public interface FinNewsService extends IService<FinNews> {

    FinNewsVO getNewsById(Long id);

    List<FinNewsVO> getNewsList();

    IPage<FinNewsVO> pageNews(Page<FinNews> page, Integer newsType);

    boolean createNews(FinNewsDTO dto);

    boolean updateNews(Long id, FinNewsDTO dto);

    boolean deleteNews(Long id);
}