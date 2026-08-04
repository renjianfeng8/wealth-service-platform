package com.wealth.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 JacksonConfig 装配路径下 PageSerializer 输出 records/total/pageNum/pageSize/pages，
 * 替代 MyBatis-Plus 默认 current/size。
 */
class PageSerializerTest {

    @Test
    void serialize_should_output_pageNum_pageSize_fields() throws Exception {
        Page<String> page = new Page<>(2, 10);
        page.setRecords(Arrays.asList("a", "b"));
        page.setTotal(35);

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        JacksonConfig config = new JacksonConfig();
        config.javaTimeModuleCustomizer().customize(builder);
        config.pageModuleCustomizer().customize(builder);
        String json = builder.build().writeValueAsString(page);

        assertTrue(json.contains("\"pageNum\":2"));
        assertTrue(json.contains("\"pageSize\":10"));
        assertTrue(json.contains("\"total\":35"));
        assertTrue(json.contains("\"pages\":4"));
        assertTrue(json.contains("\"records\":[\"a\",\"b\"]"));
        assertFalse(json.contains("\"current\""));
        assertFalse(json.contains("\"size\""));
    }
}
