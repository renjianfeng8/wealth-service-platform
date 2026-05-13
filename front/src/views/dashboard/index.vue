<template>
  <div class="finance-light">
    <div v-if="loading" class="fl-loading">
      <div class="fl-loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="fl-dashboard">
        <!-- Row 1: 核心数据卡片 -->
        <div class="fl-stats-row">
          <div class="fl-stat-card">
            <div class="fl-stat-icon fl-icon-blue">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>
            </div>
            <div class="fl-stat-body">
              <div class="fl-stat-label">资产总值 (估算)</div>
              <div class="fl-stat-value">¥{{ formatNumber(totalAsset) }}</div>
              <div class="fl-stat-change">
                <span :class="assetChange >= 0 ? 'fl-rise' : 'fl-fall'">
                  <svg v-if="assetChange >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z"/></svg>
                  <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
                  {{ Math.abs(assetChange).toFixed(2) }}%
                </span>
                <span class="fl-stat-sub">过去24小时</span>
              </div>
            </div>
          </div>
          <div class="fl-stat-card">
            <div class="fl-stat-icon fl-icon-yellow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
            </div>
            <div class="fl-stat-body">
              <div class="fl-stat-label">账户余额</div>
              <div class="fl-stat-value fl-text-yellow">¥{{ formatNumber(balanceValue) }}</div>
              <div class="fl-stat-change">
                <span :class="balanceChange >= 0 ? 'fl-rise' : 'fl-fall'">
                  <svg v-if="balanceChange >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z"/></svg>
                  <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
                  {{ Math.abs(balanceChange).toFixed(2) }}%
                </span>
                <span class="fl-stat-sub">本期变化</span>
              </div>
            </div>
          </div>
          <div class="fl-stat-card">
            <div class="fl-stat-icon fl-icon-green">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
            </div>
            <div class="fl-stat-body">
              <div class="fl-stat-label">今日收益</div>
              <div class="fl-stat-value" :class="dailyIncome >= 0 ? 'fl-text-green' : 'fl-text-red'">¥{{ formatNumber(dailyIncome) }}</div>
              <div class="fl-stat-change">
                <span :class="dailyIncome >= 0 ? 'fl-rise' : 'fl-fall'">
                  <svg v-if="dailyIncome >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z"/></svg>
                  <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
                  {{ Math.abs(dailyIncomeRate).toFixed(2) }}%
                </span>
                <span class="fl-stat-sub">日收益率</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Row 2: 资产趋势 + 余额走势 -->
        <div class="fl-chart-row">
          <div class="fl-chart-col-main">
            <div class="fl-card">
              <div class="fl-card-header">
                <div>
                  <div class="fl-card-title">资产趋势</div>
                  <div class="fl-card-subtitle">资产总值变化曲线</div>
                </div>
                <div class="fl-time-filters">
                  <button v-for="t in timeRanges" :key="t.key"
                    :class="['fl-tm-btn', { active: tmActive === t.key }]"
                    @click="tmActive = t.key; updateAssetChart()"
                  >{{ t.label }}</button>
                </div>
              </div>
              <div ref="assetChartRef" class="fl-chart-box" />
            </div>
          </div>
          <div class="fl-chart-col-side">
            <div class="fl-card">
              <div class="fl-card-header">
                <div>
                  <div class="fl-card-title">余额走势</div>
                  <div class="fl-card-subtitle">账户余额变化</div>
                </div>
                <div class="fl-time-filters">
                  <button v-for="t in balanceRanges" :key="t.key"
                    :class="['fl-tm-btn', { active: balActive === t.key }]"
                    @click="balActive = t.key; updateBalanceChart()"
                  >{{ t.label }}</button>
                </div>
              </div>
              <div ref="balanceChartRef" class="fl-chart-box" />
            </div>
          </div>
        </div>

        <!-- Row 3: 产品涨跌卡片 -->
        <div class="fl-mini-row">
          <div v-for="p in miniProducts" :key="p.productCode"
            :class="['fl-mini-card', (p.riseFallRate || 0) >= 0 ? 'fl-bdr-green' : 'fl-bdr-red']"
          >
            <div class="fl-mc-name">{{ p.productName }}</div>
            <div class="fl-mc-price" :class="(p.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">{{ formatPrice(p.price) }}</div>
            <div class="fl-mc-chg" :class="(p.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">
              <svg v-if="(p.riseFallRate || 0) >= 0" width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z"/></svg>
              <svg v-else width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
              {{ formatRate(p.riseFallRate) }}
            </div>
          </div>
        </div>

        <!-- Row 4: K线图 + 行情列表 -->
        <div class="fl-chart-row">
          <div class="fl-chart-col-kline">
            <div class="fl-card">
              <div class="fl-card-header">
                <div>
                  <div class="fl-card-title">
                    <span class="fl-symbol-name">{{ curSym?.productName || '--' }}</span>
                    <span class="fl-symbol-code">{{ curSym?.productCode || '' }}</span>
                  </div>
                  <div class="fl-card-subtitle">K线走势图</div>
                </div>
                <div class="fl-kline-controls">
                  <el-radio-group v-model="symbolSel" size="small" class="fl-symb-group">
                    <el-radio-button v-for="p in topProds" :key="p.productCode" :value="p.productCode">{{ p.productName }}</el-radio-button>
                  </el-radio-group>
                  <div class="fl-time-filters">
                    <button v-for="t in klineRanges" :key="t.key"
                      :class="['fl-tm-btn', { active: klineActive === t.key }]"
                      @click="klineActive = t.key; updateKlineChart()"
                    >{{ t.label }}</button>
                  </div>
                </div>
              </div>
              <div ref="klineChartRef" class="fl-chart-box fl-chart-kline" />
            </div>
          </div>
          <div class="fl-chart-col-list">
            <div class="fl-card fl-card-list">
              <div class="fl-card-header">
                <span class="fl-card-title">实时行情</span>
                <el-select v-model="mktFilter" size="small" placeholder="筛选" style="width:85px">
                  <el-option label="全部" value="" />
                  <el-option label="上涨" value="rise" />
                  <el-option label="下跌" value="fall" />
                </el-select>
              </div>
              <div class="fl-list-wrap">
                <div v-for="p in filteredList" :key="p.productCode" class="fl-list-row">
                  <div class="fl-list-l">
                    <div class="fl-list-name">{{ p.productName }}</div>
                    <div class="fl-list-code">{{ p.productCode }}</div>
                  </div>
                  <div class="fl-list-r">
                    <div class="fl-list-price">¥{{ formatPrice(p.price) }}</div>
                    <div class="fl-list-chg" :class="(p.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">{{ formatRate(p.riseFallRate) }}</div>
                  </div>
                </div>
                <el-empty v-if="!filteredList.length" description="暂无数据" :image-size="50" />
              </div>
            </div>
          </div>
        </div>

        <!-- Row 5: 快捷入口 -->
        <div class="fl-card">
          <div class="fl-card-header">
            <span class="fl-card-title">快捷入口</span>
          </div>
          <div class="fl-entry-grid">
            <div v-for="e in entries" :key="e.label" class="fl-entry-item" @click="$router.push(e.path)">
              <div class="fl-entry-icon" :style="{ background: e.bg }">
                <el-icon :size="20"><component :is="e.icon" /></el-icon>
              </div>
              <span class="fl-entry-label">{{ e.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getProductList } from '@/api/product'
import { formatPrice, formatRate } from '@/utils/format'
import {
  User, Goods, DataLine, List, Message,
  Star, Reading, Search, Setting,
} from '@element-plus/icons-vue'
import type { FinProduct } from '@/types'
import '@/styles/light-theme.css'

/* ============================================
   Types
   ============================================ */
interface CandleData { time: string; open: number; close: number; high: number; low: number; volume: number }

/* ============================================
   State
   ============================================ */
const loading = ref(true)
const products = ref<FinProduct[]>([])
const assetChartRef = ref<HTMLDivElement>()
const balanceChartRef = ref<HTMLDivElement>()
const klineChartRef = ref<HTMLDivElement>()

let assetChart: echarts.ECharts | null = null
let balanceChart: echarts.ECharts | null = null
let klineChart: echarts.ECharts | null = null
const observers: ResizeObserver[] = []

/* ============================================
   Time Ranges
   ============================================ */
const tmActive = ref('1D')
const timeRanges = [
  { key: '30m', label: '30m' },
  { key: '1H', label: '1H' },
  { key: '4H', label: '4H' },
  { key: '1D', label: '1D' },
]
const balActive = ref('1D')
const balanceRanges = [
  { key: '30m', label: '30m' },
  { key: '1H', label: '1H' },
  { key: '4H', label: '4H' },
  { key: '1D', label: '1D' },
]
const klineActive = ref('1D')
const klineRanges = [
  { key: '30m', label: '30m' },
  { key: '1H', label: '1H' },
  { key: '4H', label: '4H' },
  { key: '1D', label: '1D' },
]

/* ============================================
   Computed
   ============================================ */
const totalAsset = computed(() => {
  if (!products.value.length) return 0
  return products.value.reduce((s, p) => s + (p.price || 0) * (Math.floor(Math.random() * 100 + 20)), 0)
})
const assetChange = computed(() => (Math.random() - 0.35) * 10)
const balanceValue = computed(() => {
  if (!products.value.length) return 0
  return products.value.reduce((s, p) => s + (p.price || 0) * Math.floor(Math.random() * 30 + 5), 0)
})
const balanceChange = computed(() => (Math.random() - 0.4) * 6)
const dailyIncome = computed(() => {
  if (!products.value.length) return 0
  return products.value.reduce((s, p) => s + (p.price || 0) * (Math.random() - 0.45) * 0.5, 0)
})
const dailyIncomeRate = computed(() => (Math.random() - 0.35) * 4)

const topProds = computed(() => products.value.slice(0, 6))
const miniProducts = computed(() => products.value.slice(0, 12))
const symbolSel = ref('')
const curSym = computed(() => products.value.find(p => p.productCode === symbolSel.value))

const mktFilter = ref('')
const filteredList = computed(() => {
  if (!mktFilter.value) return products.value
  return products.value.filter(p => {
    const r = p.riseFallRate || 0
    return mktFilter.value === 'rise' ? r > 0 : r < 0
  })
})

function formatNumber(v: number): string {
  if (v >= 1e8) return (v / 1e8).toFixed(2) + '亿'
  if (v >= 1e4) return (v / 1e4).toFixed(2) + '万'
  return v.toFixed(2)
}

/* ============================================
   Mock data generators
   ============================================ */
function generateTrendData(base: number, count: number, volatility: number, drift: number): number[] {
  const data: number[] = []
  let v = base * 0.86
  for (let i = 0; i < count; i++) {
    v += (Math.random() - 0.46) * base * volatility
    v *= drift >= 0 ? 1.0035 : 0.997
    data.push(v)
  }
  return data
}

function generateCandleData(basePrice: number, count: number): CandleData[] {
  const data: CandleData[] = []
  let p = basePrice * 0.92
  const now = Date.now()
  for (let i = count - 1; i >= 0; i--) {
    const o = p
    const c = o + (Math.random() - 0.48) * basePrice * 0.03
    const h = Math.max(o, c) + Math.random() * basePrice * 0.01
    const l = Math.min(o, c) - Math.random() * basePrice * 0.01
    const v = Math.floor(Math.random() * 5000 + 1000)
    const t = new Date(now - i * 3600000)
    data.push({
      time: `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`,
      open: Number(o.toFixed(2)),
      close: Number(c.toFixed(2)),
      high: Number(h.toFixed(2)),
      low: Number(l.toFixed(2)),
      volume: v,
    })
    p = c
  }
  return data
}

/* ============================================
   ECharts — Light Axis Theme
   ============================================ */
function lightAxis() {
  return {
    axisLine: { lineStyle: { color: '#e4e7ed' } },
    axisTick: { lineStyle: { color: '#e4e7ed' } },
    splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' as const } },
    axisLabel: { color: '#909399', fontSize: 11 },
  }
}

function createChart(dom: HTMLDivElement): echarts.ECharts {
  const chart = echarts.init(dom, undefined, { renderer: 'canvas' })
  const observer = new ResizeObserver(() => chart.resize())
  observer.observe(dom)
  observers.push(observer)
  return chart
}

/* ============================================
   Asset Trend Chart
   ============================================ */
function buildAssetOption(): echarts.EChartsOption {
  const count = tmActive.value === '30m' ? 48 : tmActive.value === '1H' ? 24 : tmActive.value === '4H' ? 30 : 24
  const data = generateTrendData(totalAsset.value, count, 0.018, assetChange.value)
  const isUp = data[data.length - 1] >= data[0]
  const color = isUp ? '#19be6b' : '#1a6dff'

  const now = Date.now()
  const interval = tmActive.value === '30m' ? 1800000 : tmActive.value === '1H' ? 3600000 : tmActive.value === '4H' ? 14400000 : 3600000
  const times: string[] = Array.from({ length: count }, (_, i) => {
    const t = new Date(now - (count - 1 - i) * interval)
    return `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`
  })

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${p.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#303133">¥${formatNumber(p.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: {
      type: 'category',
      data: times,
      boundaryGap: false,
      ...lightAxis(),
      axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(count / 6) },
    },
    yAxis: {
      type: 'value',
      ...lightAxis(),
      axisLabel: {
        color: '#909399', fontSize: 10,
        formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : v.toFixed(0),
      },
      splitNumber: 4,
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { color, width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: isUp ? 'rgba(25,190,107,0.15)' : 'rgba(26,109,255,0.12)' },
          { offset: 1, color: isUp ? 'rgba(25,190,107,0)' : 'rgba(26,109,255,0)' },
        ]),
      },
      data,
    }],
  }
}

/* ============================================
   Balance Chart
   ============================================ */
function buildBalanceOption(): echarts.EChartsOption {
  const count = balActive.value === '30m' ? 30 : balActive.value === '1H' ? 20 : balActive.value === '4H' ? 24 : 16
  const data = generateTrendData(balanceValue.value, count, 0.015, balanceChange.value)

  const now = Date.now()
  const times: string[] = Array.from({ length: count }, (_, i) => {
    const t = new Date(now - (count - 1 - i) * 3600000)
    return `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`
  })

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${p.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#f5a623">¥${formatNumber(p.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: {
      type: 'category',
      data: times,
      boundaryGap: false,
      ...lightAxis(),
      axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(count / 4) },
    },
    yAxis: {
      type: 'value',
      ...lightAxis(),
      axisLabel: {
        color: '#909399', fontSize: 10,
        formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : v.toFixed(0),
      },
      splitNumber: 3,
    },
    series: [{
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: { color: '#f5a623', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245,166,35,0.12)' },
          { offset: 1, color: 'rgba(245,166,35,0)' },
        ]),
      },
      data,
    }],
  }
}

/* ============================================
   K-line Chart
   ============================================ */
function buildKlineOption(): echarts.EChartsOption {
  const bp = curSym.value?.price || 100
  const count = klineActive.value === '30m' ? 30 : klineActive.value === '1H' ? 24 : klineActive.value === '4H' ? 24 : 20
  const data = generateCandleData(bp, count)
  const ohlc = data.map(d => [d.open, d.close, d.low, d.high])
  const times = data.map(d => d.time)

  const ma5 = data.map((_, i, arr) => {
    if (i < 4) return '-'
    return (arr.slice(i - 4, i + 1).reduce((s, d) => s + d.close, 0) / 5).toFixed(2)
  })

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const idx = params[0].dataIndex
        const d = data[idx]
        const c = d.close >= d.open ? '#19be6b' : '#ed4014'
        return `
          <div style="font-size:12px;color:#909399;margin-bottom:6px">${d.time}</div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">开盘</span><span style="color:#303133;font-weight:500">${d.open.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">收盘</span><span style="color:${c};font-weight:600">${d.close.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最高</span><span style="color:#303133">${d.high.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最低</span><span style="color:#303133">${d.low.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">成交量</span><span style="color:#303133">${d.volume.toLocaleString()}</span>
          </div>`
      },
    },
    grid: { left: 50, right: 16, top: 12, bottom: 28 },
    xAxis: {
      type: 'category',
      data: times,
      ...lightAxis(),
      axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(count / 6) },
    },
    yAxis: {
      type: 'value',
      scale: true,
      ...lightAxis(),
      axisLabel: { color: '#909399', fontSize: 10 },
      splitNumber: 4,
    },
    dataZoom: [{ type: 'inside', start: 0, end: 100 }],
    series: [
      {
        type: 'candlestick',
        name: 'K线',
        data: ohlc,
        itemStyle: { color: '#19be6b', color0: '#ed4014', borderColor: '#19be6b', borderColor0: '#ed4014' },
      },
      {
        type: 'line',
        name: 'MA5',
        data: ma5,
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#f5a623', width: 1.5, opacity: 0.7 },
      },
    ],
  }
}

function updateAssetChart() { assetChart?.setOption(buildAssetOption(), true) }
function updateBalanceChart() { balanceChart?.setOption(buildBalanceOption(), true) }
function updateKlineChart() { klineChart?.setOption(buildKlineOption(), true) }

function initAllCharts() {
  if (assetChartRef.value) {
    assetChart = createChart(assetChartRef.value)
    assetChart.setOption(buildAssetOption())
  }
  if (balanceChartRef.value) {
    balanceChart = createChart(balanceChartRef.value)
    balanceChart.setOption(buildBalanceOption())
  }
  if (klineChartRef.value) {
    klineChart = createChart(klineChartRef.value)
    klineChart.setOption(buildKlineOption())
  }
}

function disposeAllCharts() {
  observers.forEach(o => o.disconnect())
  observers.length = 0
  assetChart?.dispose()
  balanceChart?.dispose()
  klineChart?.dispose()
  assetChart = null
  balanceChart = null
  klineChart = null
}

watch(symbolSel, () => nextTick(updateKlineChart))

const entries = [
  { label: '用户管理', path: '/user', icon: User, bg: '#1a6dff' },
  { label: '产品管理', path: '/product', icon: Goods, bg: '#19be6b' },
  { label: '行情数据', path: '/market', icon: DataLine, bg: '#f5a623' },
  { label: '交易管理', path: '/trade', icon: List, bg: '#ed4014' },
  { label: '自选管理', path: '/favorite', icon: Star, bg: '#d29922' },
  { label: '消息管理', path: '/message', icon: Message, bg: '#00b894' },
  { label: '管理员', path: '/system/admin', icon: Setting, bg: '#8b5cf6' },
  { label: 'ES 搜索', path: '/search', icon: Search, bg: '#1a6dff' },
]

async function fetchAll() {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = (res.data || []) as FinProduct[]
    if (products.value.length && !symbolSel.value) symbolSel.value = products.value[0].productCode
  } catch { /* ignore */ }
  finally {
    loading.value = false
    await nextTick()
    initAllCharts()
  }
}

onMounted(fetchAll)
onUnmounted(disposeAllCharts)
</script>

<style scoped>
/* ============================================
   浅色金融仪表盘 — 样式模块化
   ============================================ */

.fl-dashboard {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ---------- Loading ---------- */
.fl-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
  gap: 16px;
  color: #909399;
  font-size: 14px;
}
.fl-loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e4e7ed;
  border-top-color: #1a6dff;
  border-radius: 50%;
  animation: fl-spin 0.8s linear infinite;
}
@keyframes fl-spin { to { transform: rotate(360deg); } }

/* ---------- Cards ---------- */
.fl-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  padding: 18px 20px;
  transition: all 0.25s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.fl-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.fl-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
.fl-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.fl-card-subtitle {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

/* ---------- Stats Row ---------- */
.fl-stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.fl-stat-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: all 0.25s ease;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.fl-stat-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.10);
  transform: translateY(-2px);
}

.fl-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.fl-icon-blue { background: rgba(26, 109, 255, 0.08); color: #1a6dff; }
.fl-icon-yellow { background: rgba(245, 166, 35, 0.08); color: #f5a623; }
.fl-icon-green { background: rgba(25, 190, 107, 0.08); color: #19be6b; }

.fl-stat-body { flex: 1; min-width: 0; }
.fl-stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.fl-stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  font-family: 'Courier New', monospace;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fl-text-yellow { color: #f5a623; }

.fl-stat-change {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 13px;
}
.fl-stat-sub { color: #c0c4cc; }

/* ---------- Charts Row ---------- */
.fl-chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}
.fl-chart-col-main,
.fl-chart-col-side {
  display: flex;
  flex-direction: column;
}
.fl-chart-col-main .fl-card,
.fl-chart-col-side .fl-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.fl-chart-box {
  flex: 1;
  min-height: 200px;
  width: 100%;
}
.fl-chart-kline { min-height: 380px; }

/* ---------- Time Filters ---------- */
.fl-time-filters {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.fl-tm-btn {
  background: transparent;
  border: 1px solid #dcdfe6;
  color: #909399;
  padding: 3px 12px;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}
.fl-tm-btn:hover {
  border-color: #1a6dff;
  color: #1a6dff;
}
.fl-tm-btn.active {
  background: #1a6dff;
  border-color: #1a6dff;
  color: #fff;
}

/* ---------- K-line controls ---------- */
.fl-kline-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.fl-symbol-name {
  font-size: 16px;
  font-weight: 600;
  margin-right: 8px;
  color: #303133;
}
.fl-symbol-code {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

:deep(.fl-symb-group .el-radio-button__inner) {
  font-size: 11px;
  padding: 4px 10px;
}

/* ---------- Mini Cards ---------- */
.fl-mini-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(105px, 1fr));
  gap: 10px;
}
.fl-mini-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px 10px;
  transition: all 0.2s ease;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.fl-mini-card:hover {
  box-shadow: 0 3px 12px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.fl-mc-name {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fl-mc-price {
  font-size: 16px;
  font-weight: 700;
  margin-top: 6px;
  font-family: 'Courier New', monospace;
}
.fl-mc-chg {
  font-size: 12px;
  font-weight: 600;
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 2px;
}


/* ---------- K-line + List Row ---------- */
.fl-chart-col-kline {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.fl-chart-col-kline .fl-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.fl-chart-col-list {
  width: 340px;
  flex-shrink: 0;
}
.fl-card-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.fl-list-wrap {
  flex: 1;
  overflow-y: auto;
  max-height: 380px;
}
.fl-list-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.15s;
}
.fl-list-row:last-child { border-bottom: none; }
.fl-list-row:hover { background: #f8f9fb; }

.fl-list-l {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.fl-list-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.fl-list-code {
  font-size: 10px;
  color: #909399;
}
.fl-list-r { text-align: right; }
.fl-list-price {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  font-family: 'Courier New', monospace;
}
.fl-list-chg {
  font-size: 12px;
  font-weight: 600;
  margin-top: 1px;
}

.fl-list-wrap::-webkit-scrollbar { width: 4px; }
.fl-list-wrap::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }

/* ---------- Entry Grid ---------- */
.fl-entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}
.fl-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.fl-entry-item:hover { background: #f5f7fa; }
.fl-entry-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform 0.2s;
}
.fl-entry-item:hover .fl-entry-icon { transform: scale(1.08); }
.fl-entry-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

/* ---------- Responsive ---------- */
@media (max-width: 1024px) {
  .fl-chart-row { grid-template-columns: 1fr; }
  .fl-chart-col-list { width: 100%; }
  .fl-stats-row { gap: 10px; }
}
@media (max-width: 768px) {
  .fl-stats-row { grid-template-columns: 1fr; }
  .fl-card-header { flex-direction: column; align-items: flex-start; }
  .fl-kline-controls { flex-direction: column; align-items: flex-start; }
}
</style>
