package com.wealth.platform.product.service;

import com.wealth.common.dto.ProductSyncDTO;

import java.util.List;

public interface ProductSyncService {

    List<ProductSyncDTO> syncAllToES();
}
