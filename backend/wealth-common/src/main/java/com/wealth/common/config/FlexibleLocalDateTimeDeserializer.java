package com.wealth.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime 反序列化：空串视为 null（兼容表单未填日期），主格式 yyyy-MM-dd HH:mm:ss，
 * 兜底兼容 ISO-8601（带 T）旧调用。
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter MAIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text, MAIN);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(text, ISO);
            } catch (DateTimeParseException e2) {
                return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, text, "日期格式应为 yyyy-MM-dd HH:mm:ss");
            }
        }
    }
}
