package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.FinProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.vo.FinProductVO;
import java.util.List;

/**
 * 浜у搧琛ㄤ笟鍔″眰鎺ュ彛銆?
 */
public interface FinProductService extends IService<WeaProduct> {

    FinProductVO getProductById(Long id);

    List<FinProductVO> getProductList();

    IPage<FinProductVO> pageProducts(Page<WeaProduct> page, Integer productType);

    boolean createProduct(FinProductDTO dto);

    boolean updateProduct(Long id, FinProductDTO dto);

    boolean deleteProduct(Long id);
}