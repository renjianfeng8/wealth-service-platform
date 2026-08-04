package com.wealth.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 跨服务调用用的 DTO，仅包含必要字段，不暴露实体
 */
@Data
@Schema(description = "跨服务用户自选DTO")
public class UserFavoriteProviderDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
