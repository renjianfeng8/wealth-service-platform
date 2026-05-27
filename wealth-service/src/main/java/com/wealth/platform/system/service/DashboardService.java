package com.wealth.platform.system.service;

import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;

public interface DashboardService {

    DashboardOverviewVO getOverview();

    DashboardTrendVO getTrend(String period);

    DashboardKlineVO getKline(String productCode, String period);
}
