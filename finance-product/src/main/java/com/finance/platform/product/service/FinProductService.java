package com.finance.platform.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.product.dto.FinProductDTO;
import com.finance.platform.product.entity.FinProduct;
import com.finance.platform.product.vo.FinProductVO;
import java.util.List;

/**
 * 产品表业务层接口。
 */
public interface FinProductService extends IService<FinProduct> {

    FinProductVO getProductById(Long id);

    List<FinProductVO> getProductList();

    IPage<FinProductVO> pageProducts(Page<FinProduct> page, Integer productType);

    boolean createProduct(FinProductDTO dto);

    boolean updateProduct(Long id, FinProductDTO dto);

    boolean deleteProduct(Long id);
}