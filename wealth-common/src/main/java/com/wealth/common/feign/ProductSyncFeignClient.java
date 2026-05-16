package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.dto.ProductSyncDTO;
import com.wealth.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wealth-search", configuration = FeignConfig.class)
public interface ProductSyncFeignClient {

    @PostMapping("/search/product")
    Result<ProductSyncDTO> save(@RequestBody ProductSyncDTO dto);

    @DeleteMapping("/search/product/{id}")
    Result<Void> deleteById(@PathVariable("id") Long id);
}
