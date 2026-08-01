package com.wealth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.result.Result;

/**
 * Result 的 JSON 序列化工具：供 webflux（gateway）等无法复用 servlet HttpResponseUtil 的场景统一输出 Result 结构。
 */
public final class ResultJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResultJson() {
    }

    public static String write(Result<?> result) {
        try {
            return MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"code\":" + result.getCode() + ",\"message\":\"" + result.getMessage() + "\"}";
        }
    }
}
