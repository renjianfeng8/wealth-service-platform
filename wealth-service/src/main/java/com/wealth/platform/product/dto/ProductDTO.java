package com.wealth.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "产品新增/修改 DTO")
public class ProductDTO {

    @NotBlank(message = "产品名称不能为空")
    @Schema(description = "产品名称")
    private String productName;

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码")
    private String productCode;

    @Min(value = 0, message = "产品类型不能小于0")
    @Max(value = 127, message = "产品类型不能大于127")
    @Schema(description = "产品类型")
    private Integer productType;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "涨跌幅")
    private BigDecimal riseFall;

    @DecimalMin(value = "-1", message = "涨跌幅不能低于-100%")
    @DecimalMax(value = "1", message = "涨跌幅不能高于100%")
    @Schema(description = "涨跌率")
    private BigDecimal riseFallRate;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 1, message = "状态值不能大于1")
    @Schema(description = "状态")
    private Integer status;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 9999, message = "排序值不能大于9999")
    @Schema(description = "排序")
    private Integer sort;
}
