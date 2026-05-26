package com.wealth.platform.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户自选VO")
public class FinUserFavoriteVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
