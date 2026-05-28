package com.wealth.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 站内消息推送表实体 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_message")
public class WeaMessage extends BaseEntity {

    @TableField(value = "user_id")
    private Long userId;

    @TableField("message_type")
    private Integer msgType;

    @TableField("title")
    private String msgTitle;

    @TableField("content")
    private String msgContent;

    @TableField("is_read")
    private Integer readFlag;

    private LocalDateTime updateTime;
}
