package com.finance.platform.message.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FinMessageVO {
    private Long id;
    private Long userId;
    private Integer msgType;
    private String msgTitle;
    private String msgContent;
    private Integer readFlag;
    private LocalDateTime createTime;
}