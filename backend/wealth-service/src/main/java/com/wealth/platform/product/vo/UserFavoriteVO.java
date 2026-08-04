package com.wealth.platform.product.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户自选VO")
public class UserFavoriteVO {

    @Schema(description = "ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
