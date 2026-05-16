package com.wealth.common.feign;

import com.wealth.common.dto.MessageFeignDTO;
import com.wealth.common.result.Result;
import org.springframework.stereotype.Component;

@Component
public class MessageFeignClientFallback implements MessageFeignClient {

    @Override
    public Result<Boolean> createMessage(MessageFeignDTO dto) {
        return Result.success(false);
    }
}
