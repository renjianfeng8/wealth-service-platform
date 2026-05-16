package com.wealth.platform.product.service.impl;

import com.wealth.common.dto.ProductSyncDTO;
import com.wealth.common.feign.ProductSyncFeignClient;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.mapper.FinProductMapper;
import com.wealth.platform.product.service.ProductSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductSyncServiceImpl implements ProductSyncService {

    private final ProductSyncFeignClient syncFeignClient;
    private final FinProductMapper productMapper;

    public ProductSyncServiceImpl(ProductSyncFeignClient syncFeignClient, FinProductMapper productMapper) {
        this.syncFeignClient = syncFeignClient;
        this.productMapper = productMapper;
    }

    @Override
    @Scheduled(fixedRate = 1800000)
    public List<ProductSyncDTO> syncAllToES() {
        log.info("开始同步产品数据到 ES");
        List<WeaProduct> products = productMapper.selectList(null);
        List<ProductSyncDTO> synced = new ArrayList<>();

        for (WeaProduct p : products) {
            ProductSyncDTO dto = toSyncDTO(p);
            try {
                syncFeignClient.save(dto);
                synced.add(dto);
                log.debug("同步产品成功 | id={} | code={}", dto.getId(), dto.getProductCode());
            } catch (Exception e) {
                log.error("同步产品失败 | id={} | code={} | error={}",
                        dto.getId(), dto.getProductCode(), e.getMessage());
            }
        }

        log.info("产品数据同步完成 | 总数={} | 成功={}", products.size(), synced.size());
        return synced;
    }

    private ProductSyncDTO toSyncDTO(WeaProduct p) {
        ProductSyncDTO dto = new ProductSyncDTO();
        dto.setId(p.getId());
        dto.setProductName(p.getProductName());
        dto.setProductCode(p.getProductCode());
        dto.setProductType(p.getProductType());
        dto.setPrice(p.getPrice());
        dto.setRiseFall(p.getRiseFall());
        dto.setRiseFallRate(p.getRiseFallRate());
        dto.setStatus(p.getStatus());
        dto.setSort(p.getSort());
        dto.setDelFlag(p.getDelFlag());
        dto.setCreateTime(p.getCreateTime());
        dto.setUpdateTime(p.getUpdateTime());
        return dto;
    }
}
