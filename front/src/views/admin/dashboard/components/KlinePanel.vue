<template>
  <div class="fl-chart-col-kline">
    <div class="fl-card">
      <div class="fl-card-header">
        <div>
          <div class="fl-card-title">
            <span class="fl-symbol-name">{{ curSym?.productName || '--' }}</span>
            <span class="fl-symbol-code">{{ curSym?.productCode || '' }}</span>
          </div>
          <div class="fl-card-subtitle">K 线走势</div>
        </div>
        <div class="fl-kline-controls">
          <el-radio-group v-model="symbolSel" size="small" class="fl-symb-group">
            <el-radio-button
              v-for="product in topProds"
              :key="product.productCode"
              :value="product.productCode"
            >
              {{ product.productName }}
            </el-radio-button>
          </el-radio-group>
          <div class="fl-time-filters">
            <button
              v-for="range in klineRanges"
              :key="range.key"
              :class="['fl-tm-btn', { active: klineActive === range.key }]"
              type="button"
              @click="onKlineChange(range.key)"
            >
              {{ range.label }}
            </button>
          </div>
        </div>
      </div>
      <div ref="klineChartRef" class="fl-chart-box fl-chart-kline" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { Candle } from '@/api/dashboard'
import type { WeaProduct } from '@/types'

const props = defineProps<{
  products: WeaProduct[]
  klineData: Candle[]
  loadKline: (code: string, period?: string) => Promise<void>
}>()

const klineChartRef = ref<HTMLDivElement>()
const symbolSel = ref('')
const klineActive = ref('1M')
const klineRanges = [
  { key: '1D', label: '1D' },
  { key: '1W', label: '1W' },
  { key: '1M', label: '1M' },
]

const topProds = computed(() => props.products.slice(0, 6))
const curSym = computed(() => props.products.find(product => product.productCode === symbolSel.value))

let klineChart: echarts.ECharts | null = null
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

function buildKlineOption(): echarts.EChartsOption {
  const data = props.klineData
  if (!data.length) return {}
  const ohlc = data.map(point => [point.open, point.close, point.low, point.high])
  const times = data.map(point => point.time)
  const ma5 = data.map((_, index, rows) => {
    const len = Math.min(5, index + 1)
    return (rows.slice(index + 1 - len, index + 1).reduce((sum, item) => sum + item.close, 0) / len).toFixed(2)
  })

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const item = data[params[0].dataIndex]
        const color = item.close >= item.open ? '#19be6b' : '#ed4014'
        return `<div style="font-size:12px;color:#909399;margin-bottom:6px">${item.time}</div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">开盘</span><span style="color:#303133;font-weight:500">${item.open.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">收盘</span><span style="color:${color};font-weight:600">${item.close.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最高</span><span style="color:#303133">${item.high.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最低</span><span style="color:#303133">${item.low.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">成交量</span><span style="color:#303133">${item.volume.toLocaleString()}</span>
          </div>`
      },
    },
    grid: { left: 50, right: 16, top: 12, bottom: 28 },
    xAxis: { type: 'category', data: times, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(times.length / 6) } },
    yAxis: { type: 'value', scale: true, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10 }, splitNumber: 4 },
    dataZoom: [{ type: 'inside', start: 0, end: 100 }],
    series: [
      { type: 'candlestick', name: 'K线', data: ohlc, itemStyle: { color: '#19be6b', color0: '#ed4014', borderColor: '#19be6b', borderColor0: '#ed4014' } },
      { type: 'line', name: 'MA5', data: ma5, smooth: true, symbol: 'none', lineStyle: { color: '#f5a623', width: 1.5, opacity: 0.7 } },
    ],
  }
}

function updateKlineChart() {
  klineChart?.setOption(buildKlineOption(), true)
}

async function onKlineChange(period: string) {
  klineActive.value = period
  if (symbolSel.value) {
    await props.loadKline(symbolSel.value, period)
    await nextTick()
    updateKlineChart()
  }
}

function initChart() {
  const needKline = klineChartRef.value && !klineChart
  if (needKline) klineChart = createChart(klineChartRef.value)
  updateKlineChart()
  if (needKline && !klineChart) {
    requestAnimationFrame(() => nextTick(initChart))
  }
}

function disposeChart() {
  observers.forEach(observer => observer.disconnect())
  observers.length = 0
  klineChart?.dispose()
  klineChart = null
}

watch(
  () => props.products,
  async (products) => {
    if (!products.length || symbolSel.value) return
    symbolSel.value = products[0].productCode
    await props.loadKline(symbolSel.value, klineActive.value)
    await nextTick()
    initChart()
  },
  { immediate: true },
)

watch(symbolSel, async (code, previous) => {
  if (!code || code === previous) return
  await props.loadKline(code, klineActive.value)
  await nextTick()
  updateKlineChart()
})

watch(() => props.klineData, () => nextTick(updateKlineChart), { deep: true })
onMounted(() => nextTick(initChart))
onUnmounted(disposeChart)
</script>

<style scoped>
.fl-chart-col-kline {
  display: flex;
  flex-direction: column;
}

.fl-chart-col-kline .fl-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.fl-chart-box {
  flex: 1;
  min-height: 260px;
  width: 100%;
}

.fl-chart-kline {
  min-height: 400px;
}

.fl-kline-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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

.fl-symbol-name {
  font-size: 16px;
  font-weight: 600;
  margin-right: 8px;
  color: var(--fl-text);
}

.fl-symbol-code {
  font-size: 12px;
  color: var(--fl-text-dim);
  font-weight: 400;
}

:deep(.fl-symb-group .el-radio-button__inner) {
  font-size: 11px;
  padding: 4px 10px;
}

@media (max-width: 768px) {
  .fl-kline-controls {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
