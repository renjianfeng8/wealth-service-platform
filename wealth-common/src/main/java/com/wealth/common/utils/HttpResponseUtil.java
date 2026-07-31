package com.wealth.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * HTTP 响应写入工具，收敛拦截器内联的 401/403 JSON 错误响应写入。
 */
public class HttpResponseUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpResponseUtil() {
    }

    /**
     * 写入 JSON 错误响应（HTTP 状态码 + 业务码 + 消息）。
     */
    public static void writeJson(HttpServletResponse response, int status, int code, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        MAPPER.writeValue(response.getWriter(), Result.error(code, message));
    }
}
