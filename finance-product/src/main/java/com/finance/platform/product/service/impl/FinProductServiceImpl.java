package com.finance.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.product.entity.FinProduct;
import com.finance.platform.product.mapper.FinProductMapper;
import com.finance.platform.product.service.FinProductService;
import org.springframework.stereotype.Service;

/**
 * 产品表业务层实现类。
 */
@Service
public class FinProductServiceImpl extends ServiceImpl<FinProductMapper, FinProduct>
    implements FinProductService {

    /**
     * 默认构造器。
     */
    public FinProductServiceImpl() {
        // Spring 会通过无参构造创建 Bean
    }
}

