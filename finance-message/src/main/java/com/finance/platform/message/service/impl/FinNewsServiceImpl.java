package com.finance.platform.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.message.entity.FinNews;
import com.finance.platform.message.mapper.FinNewsMapper;
import com.finance.platform.message.service.FinNewsService;
import org.springframework.stereotype.Service;

/**
 * 财经资讯公告表业务层实现类。
 */
@Service
public class FinNewsServiceImpl extends ServiceImpl<FinNewsMapper, FinNews>
    implements FinNewsService {

    /**
     * 默认构造器。
     */
    public FinNewsServiceImpl() {
    }
}

