package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "瑙掕壊 VO")
public class UmsRoleVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "瑙掕壊鍚嶇О")
    private String name;

    @Schema(description = "瑙掕壊鎻忚堪")
    private String description;

    @Schema(description = "鐘舵€?0绂佺敤 1姝ｅ父")
    private Integer status;

    @Schema(description = "鎺掑簭")
    private Integer sort;

    @Schema(description = "鍒涘缓鏃堕棿")
    private LocalDateTime createTime;
}
