package com.wealth.common.contract;

import com.wealth.common.dto.MessageFeignDTO;

/**
 * 站内消息服务契约 — 供其他模块内部调用，不走 Feign HTTP
 */
public interface MessageService {
    void createMessage(MessageFeignDTO dto);
}
