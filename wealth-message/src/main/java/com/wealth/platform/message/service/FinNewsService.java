package com.wealth.platform.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.message.dto.FinNewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.vo.FinNewsVO;
import java.util.List;

/**
 * 财经资讯公告业务层接口
 */
public interface FinNewsService extends IService<WeaNews> {

    FinNewsVO getNewsById(Long id);

    List<FinNewsVO> getNewsList();

    IPage<FinNewsVO> pageNews(Page<WeaNews> page, Integer newsType);

    boolean createNews(FinNewsDTO dto);

    boolean updateNews(Long id, FinNewsDTO dto);

    boolean deleteNews(Long id);
}