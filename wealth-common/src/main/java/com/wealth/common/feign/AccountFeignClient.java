package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.result.Result;
import com.wealth.common.dto.FinUserFavoriteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 鏈嶅姟鍚嶅繀椤诲拰 Nacos 娉ㄥ唽鐨勪竴鑷?
// 娉ㄦ剰锛欶eignClient 璺緞闇€鍖呭惈鏈嶅姟绔?context-path锛?account锛?
@FeignClient(name = "wealth-account", configuration = FeignConfig.class)
public interface AccountFeignClient {

    @GetMapping("/account/WeaUserFavorite/{id}")
    Result<FinUserFavoriteDTO> getById(@PathVariable("id") Long id);

    @GetMapping("/account/WeaUserFavorite")
    Result<List<FinUserFavoriteDTO>> list();

    @GetMapping("/account/WeaUserFavorite/byUser")
    Result<List<FinUserFavoriteDTO>> getAccountByUserId(@RequestParam("userId") Long userId);
}