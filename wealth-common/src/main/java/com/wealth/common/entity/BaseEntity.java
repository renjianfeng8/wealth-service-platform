package com.wealth.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 瀹炰綋鍩虹被銆傚惈 id銆乧reateTime銆乽pdateTime銆乨elFlag 鍥涗釜鏍囧噯瀛楁銆? * 鍚勫瓙瀹炰綋鏍规嵁鑷韩琛ㄧ粨鏋勯€夋嫨缁ф壙鎴?@TableField(exist = false) 瑕嗙洊銆? */
@Data
public class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
