package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "仪表盘概况VO")
public class DashboardOverviewVO {

    @Schema(description = "资产总值（估算市值）")
    private BigDecimal totalAsset;

    @Schema(description = "资产变化率（%）")
    private BigDecimal assetChange;

    @Schema(description = "账户余额")
    private BigDecimal balanceValue;

    @Schema(description = "余额变化率（%）")
    private BigDecimal balanceChange;

    @Schema(description = "今日收益")
    private BigDecimal dailyIncome;

    @Schema(description = "日收益率（%）")
    private BigDecimal dailyIncomeRate;
}
