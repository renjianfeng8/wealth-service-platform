package com.finance.platform.account.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FinUserFavoriteVO {
    private Long id;
    private Long userId;
    private String productCode;
    private LocalDateTime createTime;
}