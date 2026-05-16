package com.wealth.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "产品新增/修改 DTO")
public class FinProductDTO {

    @NotBlank(message = "产品名称不能为空")
    @Schema(description = "产品名称")
    private String productName;

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码")
    private String productCode;

    @Schema(description = "产品类型")
    private Integer productType;

    @NotNull(message = "价格不能为空")
    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "涨跌幅")
    private BigDecimal riseFall;

    @Schema(description = "涨跌率")
    private BigDecimal riseFallRate;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}
