package com.wealth.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlexibleLocalDateTimeDeserializerTest {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(FORMAT));
        m.registerModule(module);
        return m;
    }

    @Test
    void deserialize_should_convert_empty_string_to_null() throws Exception {
        assertNull(mapper().readValue("\"\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_parse_main_format() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 15, 4, 5),
                mapper().readValue("\"2026-08-03 15:04:05\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_fallback_to_iso8601() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 10, 30, 0),
                mapper().readValue("\"2026-08-03T10:30:00\"", LocalDateTime.class));
    }

    @Test
    void serialize_should_use_space_format() throws Exception {
        assertEquals("\"2026-08-03 15:04:05\"",
                mapper().writeValueAsString(LocalDateTime.of(2026, 8, 3, 15, 4, 5)));
    }
}
