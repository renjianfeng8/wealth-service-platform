package com.finance.common.feign;

import com.finance.common.result.Result;
import com.finance.common.dto.FinUserFavoriteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 服务名必须和 Nacos 注册的一致
// 注意：FeignClient 路径需包含服务端 context-path（/account）
@FeignClient("finance-account")
public interface AccountFeignClient {

    @GetMapping("/account/finUserFavorite/{id}")
    Result<FinUserFavoriteDTO> getById(@PathVariable("id") Long id);

    @GetMapping("/account/finUserFavorite/list")
    Result<List<FinUserFavoriteDTO>> list();

    @GetMapping("/account/finUserFavorite/byUser")
    Result<List<FinUserFavoriteDTO>> getAccountByUserId(@RequestParam("userId") Long userId);
}