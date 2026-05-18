package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 权限校验 Feign 客户端 — 调用 system 模块校验当前用户是否有权访问指定 URI。
 */
@FeignClient(name = "wealth-system", configuration = FeignConfig.class)
public interface PermissionCheckFeignClient {

    @GetMapping("/system/umsAdmin/checkPermission")
    Result<Boolean> checkPermission(
            @RequestParam("uri") String uri,
            @RequestHeader("Authorization") String token);
}
