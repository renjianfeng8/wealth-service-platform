package com.wealth.platform.product.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FinUserFavoriteVO {
    private Long id;
    private Long userId;
    private String productCode;
    private LocalDateTime createTime;
}
