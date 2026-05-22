package com.wealth.platform.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.search.entity.ProductDocument;
import com.wealth.platform.search.entity.WeaProduct;
import com.wealth.platform.search.mapper.WeaProductMapper;
import com.wealth.platform.search.repository.ProductRepository;
import com.wealth.platform.search.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private ProductRepository productRepository;
    private final WeaProductMapper weaProductMapper;

    @Autowired(required = false)
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductSearchServiceImpl(WeaProductMapper weaProductMapper) {
        this.weaProductMapper = weaProductMapper;
    }

    private boolean esAvailable() {
        return productRepository != null;
    }

    @Override
    public ProductDocument save(ProductDocument document) {
        if (!esAvailable()) {
            log.warn("ES 不可用，save 降级跳过");
            return document;
        }
        try {
            return productRepository.save(document);
        } catch (Exception e) {
            log.warn("ES 不可用，save 降级跳过: {}", e.getMessage());
            return document;
        }
    }

    @Override
    public ProductDocument getById(Long id) {
        if (esAvailable()) {
            try {
                return productRepository.findById(id).orElse(null);
            } catch (Exception e) {
                log.warn("ES 不可用，降级查询 MySQL: {}", e.getMessage());
            }
        }
        WeaProduct product = weaProductMapper.selectById(id);
        return product != null ? toDocument(product) : null;
    }

    @Override
    public Page<ProductDocument> search(String keyword, Integer page, Integer size) {
        if (esAvailable()) {
            try {
                PageRequest pageRequest = PageRequest.of(page - 1, size);
                return productRepository.searchByKeyword(keyword, pageRequest);
            } catch (Exception e) {
                log.warn("ES 不可用，降级 MySQL LIKE 查询: {}", e.getMessage());
            }
        }
        return searchFromMysql(keyword, page, size);
    }

    @Override
    public void deleteById(Long id) {
        if (esAvailable()) {
            try {
                productRepository.deleteById(id);
            } catch (Exception e) {
                log.warn("ES 不可用，delete 降级跳过: {}", e.getMessage());
            }
        }
    }

    /**
     * MySQL LIKE 降级查询
     */
    private Page<ProductDocument> searchFromMysql(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<WeaProduct> wrapper = new LambdaQueryWrapper<WeaProduct>()
                .like(WeaProduct::getProductName, keyword)
                .or(w -> w.like(WeaProduct::getProductCode, keyword))
                .orderByAsc(WeaProduct::getSort);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WeaProduct> mysqlPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<WeaProduct> result =
                weaProductMapper.selectPage(mysqlPage, wrapper);

        List<ProductDocument> docs = result.getRecords().stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        return new PageImpl<>(docs, PageRequest.of(page - 1, size), result.getTotal());
    }

    private ProductDocument toDocument(WeaProduct product) {
        return BeanConvertUtil.convert(product, ProductDocument.class);
    }
}
