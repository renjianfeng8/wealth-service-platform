package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "璧勬簮 VO")
public class UmsResourceVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "璧勬簮鍚嶇О")
    private String name;

    @Schema(description = "璧勬簮URL")
    private String url;

    @Schema(description = "璧勬簮鎻忚堪")
    private String description;

    @Schema(description = "鍒嗙被ID")
    private Long categoryId;

    @Schema(description = "鍒涘缓鏃堕棿")
    private LocalDateTime createTime;
}
