package com.wealth.common.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XSS 过滤包装器 — 对请求参数、请求头和 query string 进行 HTML 标签转义。
 * 注意：JSON 请求体（@RequestBody）由 Jackson 反序列化，不在本包装器处理范围内。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return cleanXss(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = cleanXss(values[i]);
        }
        return cleaned;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> cleanedMap = new ConcurrentHashMap<>(originalMap.size());
        for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
            String[] values = entry.getValue();
            if (values == null) {
                cleanedMap.put(entry.getKey(), null);
                continue;
            }
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = cleanXss(values[i]);
            }
            cleanedMap.put(entry.getKey(), cleaned);
        }
        return cleanedMap;
    }

    @Override
    public String getQueryString() {
        return cleanXss(super.getQueryString());
    }

    @Override
    public String getHeader(String name) {
        return cleanXss(super.getHeader(name));
    }

    /**
     * 对输入进行 HTML 编码，转义特殊字符以防止 XSS 攻击。
     */
    private String cleanXss(String value) {
        if (value == null || value.isEmpty()) return value;
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
