package com.finance.platform.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.trade.entity.FinTradeOrder;
import com.finance.platform.trade.mapper.FinTradeOrderMapper;
import com.finance.platform.trade.service.FinTradeOrderService;
import org.springframework.stereotype.Service;

/**
 * 交易委托单业务层实现类。
 */
@Service
public class FinTradeOrderServiceImpl extends ServiceImpl<FinTradeOrderMapper, FinTradeOrder>
    implements FinTradeOrderService {

    /**
     * 默认构造器。
     */
    public FinTradeOrderServiceImpl() {
    }
}

