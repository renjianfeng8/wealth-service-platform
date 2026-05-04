package com.finance.platform.system.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UmsRoleVO {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
}