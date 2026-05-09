package com.finance.platform.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "finance_product")
public class ProductDocument {

    @Id
    private Long id;

    //@Field(type = FieldType.Text, analyzer = "ik_max_word")  暂时不装iK分词器
    @Field(type = FieldType.Text)
    private String productName;

    @Field(type = FieldType.Keyword)
    private String productCode;

    @Field(type = FieldType.Integer)
    private Integer productType;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal price;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal riseFall;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10000)
    private BigDecimal riseFallRate;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer sort;

    @Field(type = FieldType.Integer)
    private Integer delFlag;

    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
}