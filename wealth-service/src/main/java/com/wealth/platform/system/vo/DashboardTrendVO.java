package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "趋势图VO")
public class DashboardTrendVO {

    @Schema(description = "趋势数据点列表")
    private List<TrendPoint> series;

    @Data
    @Schema(description = "趋势数据点")
    public static class TrendPoint {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "资产总值")
        private BigDecimal assetValue;

        @Schema(description = "余额")
        private BigDecimal balanceValue;

        @Schema(description = "收益")
        private BigDecimal income;
    }
}
