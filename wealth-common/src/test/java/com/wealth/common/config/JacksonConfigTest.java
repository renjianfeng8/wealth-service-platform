package com.wealth.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 验证 JacksonConfig 在 Spring Jackson2ObjectMapperBuilder 装配路径下真实生效：
 * serializerByType/deserializerByType 与 JavaTimeModule 共存时的覆盖优先级。
 */
class JacksonConfigTest {

    private ObjectMapper mapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        Jackson2ObjectMapperBuilderCustomizer customizer = new JacksonConfig().javaTimeModuleCustomizer();
        customizer.customize(builder);
        return builder.build();
    }

    @Test
    void serialize_should_output_space_format_globally() throws Exception {
        assertEquals("\"2026-08-03 15:04:05\"",
                mapper().writeValueAsString(LocalDateTime.of(2026, 8, 3, 15, 4, 5)));
    }

    @Test
    void deserialize_should_accept_empty_string_globally() throws Exception {
        assertNull(mapper().readValue("\"\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_accept_main_format_globally() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 15, 4, 5),
                mapper().readValue("\"2026-08-03 15:04:05\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_fallback_to_iso_globally() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 10, 30, 0),
                mapper().readValue("\"2026-08-03T10:30:00\"", LocalDateTime.class));
    }
}
