package com.wealth.common.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * XSS 过滤包装器 — 对请求参数、请求头和 query string 进行 XSS 防护。
 * 仅剥离危险脚本内容，不做 HTML 实体转义，避免数据损坏。
 * 注意：JSON 请求体（@RequestBody）由 Jackson 反序列化处理（StringXssDeserializer），不在本包装器处理范围内。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile(
            "javascript:\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ON_EVENT_ATTR = Pattern.compile(
            "\\s+on\\w+\\s*=\\s*['\"].*?['\"]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
     * 剥离危险脚本内容（移除 script 标签、HTML 标签、javascript: 协议、事件属性）。
     * 不进行 HTML 实体转义，避免在输入阶段损坏正常数据。
     */
    private String cleanXss(String value) {
        if (value == null || value.isEmpty()) return value;
        String cleaned = value;
        cleaned = SCRIPT_TAG.matcher(cleaned).replaceAll("");
        cleaned = HTML_TAG.matcher(cleaned).replaceAll("");
        cleaned = JAVASCRIPT_PROTOCOL.matcher(cleaned).replaceAll("");
        cleaned = ON_EVENT_ATTR.matcher(cleaned).replaceAll("");
        return cleaned;
    }
}
