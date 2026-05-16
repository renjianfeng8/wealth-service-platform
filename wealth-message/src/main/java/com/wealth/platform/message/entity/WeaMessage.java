package com.wealth.platform.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wealth.common.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 绔欏唴娑堟伅鎺ㄩ€佽〃瀹炰綋銆? */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_message")
public class WeaMessage extends BaseEntity {

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

    /** 娑堟伅琛ㄦ棤 update_time 鍒?*/
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
