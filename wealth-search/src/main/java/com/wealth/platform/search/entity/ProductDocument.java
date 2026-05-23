package com.wealth.platform.search.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "wealth_product")
public class ProductDocument {

    @Id
    private Long id;

    @NotBlank(message = "产品名称不能为空")
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String productName;

    @NotBlank(message = "产品编码不能为空")
    @Field(type = FieldType.Keyword)
    private String productCode;

    @NotNull(message = "产品类型不能为空")
    @Field(type = FieldType.Integer)
    private Integer productType;

    @NotNull(message = "价格不能为空")
    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal price;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal riseFall;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal riseFallRate;

    @NotNull(message = "状态不能为空")
    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer sort;

    @NotNull(message = "删除标识不能为空")
    @Field(type = FieldType.Integer)
    private Integer delFlag;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updateTime;
}
