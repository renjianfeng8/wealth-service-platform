package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 注意：FeignClient 路径需包含服务端 context-path (/product)
@FeignClient(name = "wealth-product", configuration = FeignConfig.class)
public interface ProductFeignClient {

    @GetMapping("/product/WeaProduct/{id}")
    Result<?> getProductById(@PathVariable("id") Long id);
}