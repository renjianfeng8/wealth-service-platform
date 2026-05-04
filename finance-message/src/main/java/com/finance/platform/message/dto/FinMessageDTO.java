package com.finance.platform.message.dto;

import lombok.Data;

@Data
public class FinMessageDTO {
    private Long userId;
    private Integer msgType;
    private String msgTitle;
    private String msgContent;
}