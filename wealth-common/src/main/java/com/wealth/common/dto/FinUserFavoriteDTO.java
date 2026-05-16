package com.wealth.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 璺ㄦ湇鍔¤皟鐢ㄧ敤鐨?DTO锛屼粎鍖呭惈蹇呰瀛楁锛屼笉鏆撮湶瀹炰綋
 */
@Data
public class FinUserFavoriteDTO {
    private Long id;
    private Long userId;
    private String productCode;
    private LocalDateTime createTime;
}