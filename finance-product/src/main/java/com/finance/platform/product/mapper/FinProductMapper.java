package com.finance.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.platform.product.entity.FinProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品表数据访问层。
 */
@Mapper
public interface FinProductMapper extends BaseMapper<FinProduct> {
}

