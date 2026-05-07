package com.finance.common.feign;

import com.finance.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 注意：FeignClient 路径需包含服务端 context-path（/product）
@FeignClient("finance-product")
public interface ProductFeignClient {

    @GetMapping("/product/finProduct/{id}")
    Result<?> getProductById(@PathVariable("id") Long id);
}