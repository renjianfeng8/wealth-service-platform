package com.finance.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.finance.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息推送表实体。
 */
@Data
@TableName("fin_message")
public class FinMessage extends BaseEntity {

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "msg_type")
    private Integer msgType;

    @TableField(value = "msg_title")
    private String msgTitle;

    @TableField(value = "msg_content")
    private String msgContent;

    @TableField(value = "read_flag")
    private Integer readFlag;

    /** 消息表无 update_time 列 */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;
}
