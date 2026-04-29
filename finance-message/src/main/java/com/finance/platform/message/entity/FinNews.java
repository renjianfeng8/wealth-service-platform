package com.finance.platform.message.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 财经资讯公告表实体。
 */
@Data
@TableName("fin_news")
public class FinNews {

    /**
     * 默认构造器。
     */
    public FinNews() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资讯标题。
     */
    @TableField(value = "title")
    private String title;

    /**
     * 资讯内容。
     */
    @TableField(value = "content")
    private String content;

    /**
     * 资讯类型：1行情快讯 2行业公告 3理财资讯。
     */
    @TableField(value = "news_type")
    private Integer newsType;

    /**
     * 来源。
     */
    @TableField(value = "source")
    private String source;

    /**
     * 状态：0草稿 1已发布。
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 发布时间。
     */
    @TableField(value = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 逻辑删除标识：0未删除 1已删除。
     */
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

