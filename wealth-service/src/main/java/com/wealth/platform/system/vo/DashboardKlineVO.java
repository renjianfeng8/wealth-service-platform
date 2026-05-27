package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "K线图VO")
public class DashboardKlineVO {

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "K线数据")
    private List<Candle> candles;

    @Data
    @Schema(description = "K线数据点")
    public static class Candle {
        @Schema(description = "时间")
        private String time;

        @Schema(description = "开盘价")
        private BigDecimal open;

        @Schema(description = "最高价")
        private BigDecimal high;

        @Schema(description = "最低价")
        private BigDecimal low;

        @Schema(description = "收盘价")
        private BigDecimal close;

        @Schema(description = "成交量")
        private Long volume;
    }
}
