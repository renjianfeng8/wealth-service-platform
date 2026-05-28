package com.wealth.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 财经资讯公告表实体 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_news")
public class WeaNews extends BaseEntity {

    @TableField(value = "title")
    private String title;

    @TableField(value = "content")
    private String content;

    @TableField(value = "news_type")
    private Integer newsType;

    @TableField("author")
    private String source;

    @TableField(exist = false)
    private Integer status;

    @TableField(exist = false)
    private LocalDateTime publishTime;

    private LocalDateTime updateTime;
}
