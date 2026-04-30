package com.finance.platform.search.service;

import com.finance.platform.search.entity.ProductDocument;
import org.springframework.data.domain.Page;

public interface ProductSearchService {

    ProductDocument save(ProductDocument document);

    ProductDocument getById(Long id);

    Page<ProductDocument> search(String keyword, Integer page, Integer size);

    void deleteById(Long id);
}