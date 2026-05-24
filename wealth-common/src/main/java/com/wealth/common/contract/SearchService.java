package com.wealth.common.contract;

import com.wealth.common.dto.ProductSyncDTO;

/**
 * ES 搜索服务契约 — 供其他模块内部调用，不走 Feign HTTP
 */
public interface SearchService {
    void saveProduct(ProductSyncDTO dto);
    void deleteProduct(Long id);
}
