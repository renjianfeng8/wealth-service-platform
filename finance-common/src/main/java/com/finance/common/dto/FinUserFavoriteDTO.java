package com.finance.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 跨服务调用用的 DTO，仅包含必要字段，不暴露实体
 */
@Data
public class FinUserFavoriteDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private LocalDateTime createTime;
}