package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.product.dto.FinProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.mapper.FinProductMapper;
import com.wealth.platform.product.service.FinProductService;
import com.wealth.platform.product.service.ProductSyncService;
import com.wealth.platform.product.vo.FinProductVO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinProductServiceImpl extends ServiceImpl<FinProductMapper, WeaProduct>
        implements FinProductService {

    private final ProductSyncService productSyncService;

    public FinProductServiceImpl(ProductSyncService productSyncService) {
        this.productSyncService = productSyncService;
    }

    @Override
    public FinProductVO getProductById(Long id) {
        WeaProduct entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinProductVO vo = BeanConvertUtil.convert(entity, FinProductVO.class);
        return vo;
    }

    @Override
    public List<FinProductVO> getProductList() {
        List<WeaProduct> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> {
            FinProductVO vo = BeanConvertUtil.convert(entity, FinProductVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createProduct(FinProductDTO dto) {
        WeaProduct entity = BeanConvertUtil.convert(dto, WeaProduct.class);
        boolean saved = save(entity);
        if (saved) {
            productSyncService.syncSingleToES(entity);
        }
        return saved;
    }

    @Override
    public IPage<FinProductVO> pageProducts(Page<WeaProduct> page, Integer productType) {
        LambdaQueryWrapper<WeaProduct> wrapper = new LambdaQueryWrapper<>();
        if (productType != null && productType > 0) {
            wrapper.eq(WeaProduct::getProductType, productType);
        }
        wrapper.orderByAsc(WeaProduct::getSort);

        IPage<WeaProduct> entityPage = page(page, wrapper);
        Page<FinProductVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinProductVO.class));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long id, FinProductDTO dto) {
        WeaProduct entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "产品不存在");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        boolean updated = updateById(entity);
        if (updated) {
            productSyncService.syncSingleToES(entity);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long id) {
        boolean removed = removeById(id);
        if (removed) {
            productSyncService.deleteFromES(id);
        }
        return removed;
    }
}