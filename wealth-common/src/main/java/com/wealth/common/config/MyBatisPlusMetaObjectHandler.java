package com.wealth.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 瀛楁鑷姩濉厖澶勭悊鍣細
 * create_time / update_time 瀵瑰簲鍒板疄浣撶殑 createTime / updateTime銆?
 * 鏍囨敞 @ConditionalOnClass 浣垮緱鏈紩鍏?MyBatis-Plus 鐨勬ā鍧楋紙濡?finance-search锛変笉浼氬洜鎵弿鍒版绫昏€屽惎鍔ㄥけ璐ャ€?
 */
@Slf4j
@Component
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.handlers.MetaObjectHandler")
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 浠呭湪瀹炰綋瀛楁瀛樺湪涓旈厤缃簡 @TableField(fill=...) 鏃剁敓鏁?
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 浠呭湪瀹炰綋瀛楁瀛樺湪涓旈厤缃簡 @TableField(fill=...) 鏃剁敓鏁?
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
