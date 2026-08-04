package com.wealth.platform.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.platform.product.entity.WeaProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品表数据访问层。
 */
@Mapper
public interface ProductMapper extends BaseMapper<WeaProduct> {
}

