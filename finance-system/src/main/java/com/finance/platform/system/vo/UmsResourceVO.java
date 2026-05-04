package com.finance.platform.system.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UmsResourceVO {
    private Long id;
    private String name;
    private String url;
    private String description;
    private Long categoryId;
    private LocalDateTime createTime;
}