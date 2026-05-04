package com.finance.platform.system.dto;

import lombok.Data;

@Data
public class UmsResourceDTO {
    private Long id;
    private String name;
    private String url;
    private String description;
    private Long categoryId;
}