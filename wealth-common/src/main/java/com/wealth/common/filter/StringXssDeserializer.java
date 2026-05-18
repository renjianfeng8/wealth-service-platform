package com.wealth.common.filter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Jackson 字符串反序列化器 — 在 JSON 反序列化时对 String 字段进行 XSS 过滤。
 * <p>
 * 自动注册到全局 ObjectMapper 中，对所有 {@code @RequestBody} 的 String 字段生效。
 */
@Slf4j
public class StringXssDeserializer extends JsonDeserializer<String> {

    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile(
            "javascript:\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ON_EVENT_ATTR = Pattern.compile(
            "\\s+on\\w+\\s*=\\s*['\"].*?['\"]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return value;
        }
        String cleaned = value;
        cleaned = SCRIPT_TAG.matcher(cleaned).replaceAll("");
        cleaned = HTML_TAG.matcher(cleaned).replaceAll("");
        cleaned = JAVASCRIPT_PROTOCOL.matcher(cleaned).replaceAll("");
        cleaned = ON_EVENT_ATTR.matcher(cleaned).replaceAll("");
        if (!cleaned.equals(value)) {
            log.debug("XSS 过滤: 请求体字符串字段已清理 | field={}", p.currentName());
        }
        return cleaned;
    }
}
