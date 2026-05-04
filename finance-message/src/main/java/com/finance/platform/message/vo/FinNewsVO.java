package com.finance.platform.message.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FinNewsVO {
    private Long id;
    private String title;
    private String content;
    private Integer newsType;
    private String source;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}