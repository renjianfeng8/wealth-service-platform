package com.wealth.platform.trade.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "交易委托VO")
public class TradeOrderVO {

    @Schema(description = "ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "交易类型")
    private Integer tradeType;

    @Schema(description = "委托价格")
    private BigDecimal entrustPrice;

    @Schema(description = "委托数量")
    private Integer entrustNum;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
