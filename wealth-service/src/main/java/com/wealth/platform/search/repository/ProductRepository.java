package com.wealth.platform.search.repository;

import com.wealth.platform.search.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    // 鍏ㄦ枃妫€绱細浜у搧鍚嶇О锛圛K鍒嗚瘝鍣ㄤ腑鏂囨绱級+ 浜у搧缂栫爜鍖归厤
    @Query("{\"bool\": {\"should\": [{\"match\": {\"productName\": {\"query\": \"?0\", \"analyzer\": \"ik_smart\"}}}, {\"term\": {\"productCode\": {\"value\": \"?0\"}}}]}}")
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);
}
