package com.finance.platform.system.dto;

import lombok.Data;

@Data
public class UmsRoleDTO {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
}