package com.wealth.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器：
 * create_time / update_time 对应到实体的 createTime / updateTime。
 * 标注 @ConditionalOnClass 使得未引入 MyBatis-Plus 的模块（如 wealth-search）不会因扫描到此类而启动失败。
 */
@Slf4j
@Component
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.handlers.MetaObjectHandler")
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 仅在实体字段存在且配置了 @TableField(fill=...) 时生效
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 仅在实体字段存在且配置了 @TableField(fill=...) 时生效
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
