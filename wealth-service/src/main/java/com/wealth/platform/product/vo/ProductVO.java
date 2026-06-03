package com.wealth.platform.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "产品VO")
public class ProductVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "产品类型")
    private Integer productType;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "涨跌额")
    private BigDecimal riseFall;

    @Schema(description = "涨跌幅")
    private BigDecimal riseFallRate;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
