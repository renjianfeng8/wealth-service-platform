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
 * 站内消息推送表实体。
 */
@Data
@TableName("fin_message")
public class FinMessage {

    /**
     * 默认构造器。
     */
    public FinMessage() {
    }

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID：0全局消息。
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 消息类型：1行情提醒 2资讯推送 3委托通知。
     */
    @TableField(value = "msg_type")
    private Integer msgType;

    /**
     * 消息标题。
     */
    @TableField(value = "msg_title")
    private String msgTitle;

    /**
     * 消息内容。
     */
    @TableField(value = "msg_content")
    private String msgContent;

    /**
     * 阅读状态：0未读 1已读。
     */
    @TableField(value = "read_flag")
    private Integer readFlag;

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

