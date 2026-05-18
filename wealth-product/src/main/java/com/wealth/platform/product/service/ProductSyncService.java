package com.wealth.platform.product.service;

import com.wealth.common.dto.ProductSyncDTO;
import com.wealth.platform.product.entity.WeaProduct;

import java.util.List;

public interface ProductSyncService {

    /** 全量同步所有产品到 ES（定时任务调用） */
    List<ProductSyncDTO> syncAllToES();

    /** 实时同步单个产品到 ES（CUD 操作后调用） */
    void syncSingleToES(WeaProduct product);

    /** 从 ES 删除单个产品 */
    void deleteFromES(Long productId);
}
