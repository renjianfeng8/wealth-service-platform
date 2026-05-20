package com.wealth.common.feign;

import com.wealth.common.config.FeignConfig;
import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wealth-message", configuration = FeignConfig.class,
        fallback = MessageFeignClientFallback.class)
public interface MessageFeignClient {

    @PostMapping("/message/wea-message")
    Result<Boolean> createMessage(@RequestBody MessageFeignDTO dto);
}
