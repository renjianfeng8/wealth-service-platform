package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 娉ㄦ剰锛欶eignClient 璺緞闇€鍖呭惈鏈嶅姟绔?context-path锛?product锛?
@FeignClient(name = "wealth-product", configuration = FeignConfig.class)
public interface ProductFeignClient {

    @GetMapping("/product/WeaProduct/{id}")
    Result<?> getProductById(@PathVariable("id") Long id);
}