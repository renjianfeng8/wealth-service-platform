package com.wealth.platform.search.service.impl;

import com.wealth.common.contract.SearchService;
import com.wealth.common.dto.ProductSyncDTO;
import com.wealth.platform.search.entity.ProductDocument;
import com.wealth.platform.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchDelegateService implements SearchService {

    private final ProductSearchService productSearchService;

    @Override
    public void saveProduct(ProductSyncDTO dto) {
        ProductDocument doc = new ProductDocument();
        doc.setId(dto.getId());
        doc.setProductName(dto.getProductName());
        doc.setProductCode(dto.getProductCode());
        doc.setProductType(dto.getProductType());
        doc.setPrice(dto.getPrice());
        doc.setRiseFall(dto.getRiseFall());
        doc.setRiseFallRate(dto.getRiseFallRate());
        doc.setStatus(dto.getStatus());
        doc.setSort(dto.getSort());
        doc.setDelFlag(dto.getDelFlag());
        doc.setCreateTime(dto.getCreateTime());
        doc.setUpdateTime(dto.getUpdateTime());
        productSearchService.save(doc);
    }

    @Override
    public void deleteProduct(Long id) {
        productSearchService.deleteById(id);
    }
}
