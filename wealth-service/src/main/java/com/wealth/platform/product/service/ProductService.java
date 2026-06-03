package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.ProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.vo.ProductVO;
import java.util.List;

/**
 * 产品表业务层接口。
 */
public interface ProductService extends IService<WeaProduct> {

    ProductVO getProductById(Long id);

    List<ProductVO> getProductList();

    IPage<ProductVO> pageProducts(Page<WeaProduct> page, String productName, String productCode, Integer productType, String orderBy, String orderDir);

    boolean createProduct(ProductDTO dto);

    boolean updateProduct(Long id, ProductDTO dto);

    boolean deleteProduct(Long id);
}