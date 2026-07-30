package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.platform.system.service.DashboardService;
import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "仪表盘管理", description = "管理后台仪表盘数据接口")
@RequestMapping("/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘概况（资产总值、账户余额、今日收益等）")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @Operation(summary = "获取趋势图数据")
    @GetMapping("/trend")
    public Result<DashboardTrendVO> trend(
            @RequestParam(defaultValue = "7D") String period) {
        return Result.success(dashboardService.getTrend(period));
    }

    @Operation(summary = "获取K线图数据")
    @GetMapping("/kline/{productCode}")
    public Result<DashboardKlineVO> kline(
            @PathVariable String productCode,
            @RequestParam(defaultValue = "1D") String period) {
        return Result.success(dashboardService.getKline(productCode, period));
    }
}
