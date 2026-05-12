package com.finance.platform.search.repository;

import com.finance.platform.search.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    // 全文检索：产品名称（IK分词器中文检索）+ 产品编码匹配
    @Query("{\"bool\": {\"should\": [{\"match\": {\"productName\": {\"query\": \"?0\", \"analyzer\": \"ik_smart\"}}}, {\"term\": {\"productCode\": {\"value\": \"?0\"}}}]}}")
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);
}
