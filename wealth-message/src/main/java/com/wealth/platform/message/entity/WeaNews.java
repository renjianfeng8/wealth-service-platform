package com.wealth.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 璐㈢粡璧勮鍏憡琛ㄥ疄浣撱€? */
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

    @TableField(value = "source")
    private String source;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "publish_time")
    private LocalDateTime publishTime;

    /** 璧勮琛ㄦ棤 update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
