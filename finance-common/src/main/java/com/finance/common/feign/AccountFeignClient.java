package com.finance.common.feign;

import com.finance.common.result.Result;
import com.finance.common.dto.FinUserFavoriteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 服务名必须和 Nacos 注册的一致
@FeignClient("finance-account")
public interface AccountFeignClient {

    // 路径和 Controller 接口完全一致，返回类型用 DTO
    @GetMapping("/finUserFavorite/{id}")
    Result<FinUserFavoriteDTO> getById(@PathVariable("id") Long id);
}