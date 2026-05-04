package com.finance.platform.message.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FinNewsDTO {
    private String title;
    private String content;
    private Integer newsType;
    private String source;
    private Integer status;
    private LocalDateTime publishTime;
}