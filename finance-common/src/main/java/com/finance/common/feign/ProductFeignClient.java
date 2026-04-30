package com.finance.common.feign;

import com.finance.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("finance-product")
public interface ProductFeignClient {

    @GetMapping("/product/{id}")
    Result getProductById(@PathVariable("id") Long id);
}