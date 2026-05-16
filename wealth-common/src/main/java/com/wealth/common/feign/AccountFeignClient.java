package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.result.Result;
import com.wealth.common.dto.FinUserFavoriteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 服务名必须和 Nacos 注册的一致
// 注意：FeignClient 路径需包含服务端 context-path (/account)
@FeignClient(name = "wealth-account", configuration = FeignConfig.class)
public interface AccountFeignClient {

    @GetMapping("/account/WeaUserFavorite/{id}")
    Result<FinUserFavoriteDTO> getById(@PathVariable("id") Long id);

    @GetMapping("/account/WeaUserFavorite")
    Result<List<FinUserFavoriteDTO>> list();

    @GetMapping("/account/WeaUserFavorite/byUser")
    Result<List<FinUserFavoriteDTO>> getAccountByUserId(@RequestParam("userId") Long userId);
}