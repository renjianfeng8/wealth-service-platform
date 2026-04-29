package com.finance.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.product.entity.FinMarketData;
import com.finance.platform.product.mapper.FinMarketDataMapper;
import com.finance.platform.product.service.FinMarketDataService;
import org.springframework.stereotype.Service;

/**
 * 行情数据表业务层实现类。
 */
@Service
public class FinMarketDataServiceImpl extends ServiceImpl<FinMarketDataMapper, FinMarketData>
    implements FinMarketDataService {

    /**
     * 默认构造器。
     */
    public FinMarketDataServiceImpl() {
    }
}

