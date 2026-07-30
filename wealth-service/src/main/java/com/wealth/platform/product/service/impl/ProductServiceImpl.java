package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.product.dto.ProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.mapper.ProductMapper;
import com.wealth.platform.product.service.ProductService;
import com.wealth.platform.product.vo.ProductVO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.LikeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, WeaProduct>
        implements ProductService {

    @Override
    public ProductVO getProductById(Long id) {
        WeaProduct entity = getById(id);
        if (entity == null) {
            return null;
        }
        ProductVO vo = BeanConvertUtil.convert(entity, ProductVO.class);
        return vo;
    }

    @Override
    public List<ProductVO> getProductList(Integer pageNum, Integer pageSize) {
        List<WeaProduct> list = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> {
            ProductVO vo = BeanConvertUtil.convert(entity, ProductVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createProduct(ProductDTO dto) {
        long count = lambdaQuery().eq(WeaProduct::getProductCode, dto.getProductCode()).count();
        if (count > 0) {
            throw new ServiceException(400, "产品编码已存在");
        }
        WeaProduct entity = BeanConvertUtil.convert(dto, WeaProduct.class);
        return save(entity);
    }

    @Override
    public IPage<ProductVO> pageProducts(Page<WeaProduct> page, String productName, String productCode, Integer productType, String orderBy, String orderDir) {
        LambdaQueryWrapper<WeaProduct> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(productName) || StringUtils.hasText(productCode)) {
            wrapper.and(w -> {
                if (StringUtils.hasText(productName)) {
                    w.like(WeaProduct::getProductName, LikeUtil.escape(productName));
                }
                if (StringUtils.hasText(productCode)) {
                    if (StringUtils.hasText(productName)) {
                        w.or();
                    }
                    w.like(WeaProduct::getProductCode, LikeUtil.escape(productCode));
                }
            });
        }
        if (productType != null) {
            wrapper.eq(WeaProduct::getProductType, productType);
        }
        // Dynamic sorting
        if ("price".equals(orderBy)) {
            wrapper.orderBy(true, !"desc".equals(orderDir), WeaProduct::getPrice);
        } else if ("riseFallRate".equals(orderBy)) {
            wrapper.orderBy(true, !"desc".equals(orderDir), WeaProduct::getRiseFallRate);
        } else {
            wrapper.orderByAsc(WeaProduct::getSort);
        }
        return BeanConvertUtil.convertPage(baseMapper.selectPage(page, wrapper), ProductVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long id, ProductDTO dto) {
        WeaProduct entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "产品不存在");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long id) {
        if (getById(id) == null) {
            throw new ServiceException(404, "产品不存在");
        }
        return removeById(id);
    }
}
