package com.finance.platform.search.repository;

import com.finance.platform.search.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    // 全文检索：产品名称 + 编码
    Page<ProductDocument> findByProductNameContainingOrProductCodeContaining(
            String name, String code, Pageable pageable);
}