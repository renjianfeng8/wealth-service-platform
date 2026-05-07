package com.finance.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 财经资讯公告表实体。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("fin_news")
public class FinNews extends BaseEntity {

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

    /** 资讯表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;
}
