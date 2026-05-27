import request from './index'

/**
 * 仪表盘概览数据
 */
export interface DashboardOverview {
  totalAsset: number
  assetChange: number
  balanceValue: number
  balanceChange: number
  dailyIncome: number
  dailyIncomeRate: number
}

/**
 * 趋势数据点
 */
export interface TrendPoint {
  date: string
  assetValue: number
  balanceValue: number
  income: number
}

/**
 * 仪表盘趋势数据
 */
export interface DashboardTrend {
  series: TrendPoint[]
}

/**
 * K 线数据点
 */
export interface Candle {
  time: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

/**
 * 仪表盘 K 线数据
 */
export interface DashboardKline {
  productCode: string
  productName: string
  candles: Candle[]
}

/**
 * 获取仪表盘概览数据
 * @returns 总资产、可用余额、日收益等概览信息
 */
export function getDashboardOverview() {
  return request.get<DashboardOverview>('/system/dashboard/overview')
}

/**
 * 获取仪表盘趋势数据
 * @param period - 周期（如 week、month、year）
 * @returns 资产/余额/收益趋势序列
 */
export function getDashboardTrend(period: string) {
  return request.get<DashboardTrend>('/system/dashboard/trend', { params: { period } })
}

/**
 * 获取产品 K 线图数据
 * @param productCode - 产品代码
 * @param period - 周期（如 day、week、month）
 * @returns K 线数据序列
 */
export function getDashboardKline(productCode: string, period: string) {
  return request.get<DashboardKline>(`/system/dashboard/kline/${productCode}`, { params: { period } })
}
