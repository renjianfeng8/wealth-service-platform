<template>
  <div class="fl-chart-row">
    <div class="fl-chart-col-main">
      <div class="fl-card">
        <div class="fl-card-header">
          <div>
            <div class="fl-card-title">资产趋势</div>
            <div class="fl-card-subtitle">资产总值变化曲线</div>
          </div>
          <div class="fl-time-filters">
            <button
              v-for="range in timeRanges"
              :key="range.key"
              :class="['fl-tm-btn', { active: tmActive === range.key }]"
              type="button"
              @click="onTmChange(range.key)"
            >
              {{ range.label }}
            </button>
          </div>
        </div>
        <div ref="assetChartRef" class="fl-chart-box" />
      </div>
    </div>

    <div class="fl-chart-col-side">
      <div class="fl-card">
        <div class="fl-card-header">
          <div>
            <div class="fl-card-title">余额趋势</div>
            <div class="fl-card-subtitle">账户余额变化</div>
          </div>
          <div class="fl-time-filters">
            <button
              v-for="range in balanceRanges"
              :key="range.key"
              :class="['fl-tm-btn', { active: balActive === range.key }]"
              type="button"
              @click="onBalChange(range.key)"
            >
              {{ range.label }}
            </button>
          </div>
        </div>
        <div ref="balanceChartRef" class="fl-chart-box" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

interface TrendPoint {
  date: string
  assetValue: number
  balanceValue: number
  income: number
}

const props = defineProps<{
  trendData: { series: TrendPoint[] } | null
  loadTrend: (period?: string) => Promise<void>
  formatNumber: (value: number) => string
}>()

const assetChartRef = ref<HTMLDivElement>()
const balanceChartRef = ref<HTMLDivElement>()
const tmActive = ref('7D')
const balActive = ref('7D')
const timeRanges = [
  { key: '7D', label: '7D' },
  { key: '30D', label: '30D' },
]
const balanceRanges = [
  { key: '7D', label: '7D' },
  { key: '30D', label: '30D' },
]

let assetChart: echarts.ECharts | null = null
let balanceChart: echarts.ECharts | null = null
const observers: ResizeObserver[] = []

function lightAxis() {
  return {
    axisLine: { lineStyle: { color: '#e4e7ed' } },
    axisTick: { lineStyle: { color: '#e4e7ed' } },
    splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' as const } },
    axisLabel: { color: '#909399', fontSize: 11 },
  }
}

function createChart(dom: HTMLDivElement): echarts.ECharts | null {
  if (!dom.clientWidth || !dom.clientHeight) return null
  const chart = echarts.init(dom, undefined, { renderer: 'canvas' })
  const observer = new ResizeObserver(() => chart.resize())
  observer.observe(dom)
  observers.push(observer)
  return chart
}

function buildAssetOption(): echarts.EChartsOption {
  const data = props.trendData?.series?.map(point => point.assetValue) ?? []
  const times = props.trendData?.series?.map(point => point.date) ?? []
  if (!data.length) return {}
  const isUp = data[data.length - 1] >= data[0]
  const color = isUp ? '#19be6b' : '#1a6dff'

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const item = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${item.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#303133">¥${props.formatNumber(item.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: { type: 'category', data: times, boundaryGap: false, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(data.length / 6) } },
    yAxis: { type: 'value', ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, formatter: (value: number) => value >= 10000 ? `${(value / 10000).toFixed(0)}万` : value.toFixed(0) }, splitNumber: 4 },
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

function buildBalanceOption(): echarts.EChartsOption {
  const data = props.trendData?.series?.map(point => point.balanceValue) ?? []
  const times = props.trendData?.series?.map(point => point.date) ?? []
  if (!data.length) return {}

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const item = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${item.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#f5a623">¥${props.formatNumber(item.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: { type: 'category', data: times, boundaryGap: false, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(data.length / 4) } },
    yAxis: { type: 'value', ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, formatter: (value: number) => value >= 10000 ? `${(value / 10000).toFixed(0)}万` : value.toFixed(0) }, splitNumber: 3 },
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

function updateCharts() {
  assetChart?.setOption(buildAssetOption(), true)
  balanceChart?.setOption(buildBalanceOption(), true)
}

async function onTmChange(period: string) {
  tmActive.value = period
  await props.loadTrend(period)
  await nextTick()
  updateCharts()
}

async function onBalChange(period: string) {
  balActive.value = period
  await props.loadTrend(period)
  await nextTick()
  updateCharts()
}

function initCharts() {
  const needAsset = assetChartRef.value && !assetChart
  const needBalance = balanceChartRef.value && !balanceChart
  if (needAsset) assetChart = createChart(assetChartRef.value)
  if (needBalance) balanceChart = createChart(balanceChartRef.value)
  updateCharts()
  // Retry if any container wasn't laid out yet
  if ((needAsset && !assetChart) || (needBalance && !balanceChart)) {
    requestAnimationFrame(() => nextTick(initCharts))
  }
}

function disposeCharts() {
  observers.forEach(observer => observer.disconnect())
  observers.length = 0
  assetChart?.dispose()
  balanceChart?.dispose()
  assetChart = null
  balanceChart = null
}

watch(() => props.trendData, () => nextTick(updateCharts), { deep: true })
onMounted(() => nextTick(initCharts))
onUnmounted(disposeCharts)
</script>

<style scoped>
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
  min-height: 260px;
  width: 100%;
}

.fl-time-filters {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.fl-tm-btn {
  background: transparent;
  border: 1px solid var(--fl-border-light);
  color: var(--fl-text-dim);
  padding: 3px 12px;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}

.fl-tm-btn:hover {
  border-color: var(--fl-primary);
  color: var(--fl-primary);
}

.fl-tm-btn.active {
  background: var(--fl-primary);
  border-color: var(--fl-primary);
  color: #fff;
}

@media (max-width: 1024px) {
  .fl-chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
