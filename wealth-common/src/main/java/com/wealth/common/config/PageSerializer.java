package com.wealth.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 分页响应统一为 records/total/pageNum/pageSize/pages，
 * 与请求入参 pageNum/pageSize 命名对齐（替代 MyBatis-Plus 默认 current/size）。
 */
public class PageSerializer extends JsonSerializer<Page<?>> {

    @Override
    public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("records", page.getRecords());
        gen.writeNumberField("total", page.getTotal());
        gen.writeNumberField("pageNum", page.getCurrent());
        gen.writeNumberField("pageSize", page.getSize());
        gen.writeNumberField("pages", page.getPages());
        gen.writeEndObject();
    }
}
