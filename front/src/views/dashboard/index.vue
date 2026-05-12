<template>
  <div class="dark-dashboard">
    <!-- Loading -->
    <div v-if="loading" class="db-loading">
      <el-icon class="spin" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- ==================== 趋势图行：左 资产 2/3 + 右 余额 1/3 ==================== -->
      <el-row :gutter="10" class="db-section eq-height-row">
        <!-- 左侧：资产总值趋势 -->
        <el-col :xs="24" :md="16">
          <el-card class="db-card" shadow="never">
            <div class="trend-header">
              <div>
                <div class="trend-label">资产总值 (估算)</div>
                <div class="trend-value">¥{{ formatNumber(totalAsset) }}</div>
                <div class="trend-sub">
                  <span :class="assetChange >= 0 ? 'rise' : 'fall'">
                    <el-icon :size="12"><Top v-if="assetChange >= 0" /><Bottom v-else /></el-icon>
                    {{ Math.abs(assetChange).toFixed(2) }}%
                  </span>
                  <span class="sub-muted">过去24小时</span>
                </div>
              </div>
              <div class="time-filters">
                <button
                  v-for="t in timeRanges" :key="t.key"
                  :class="['tm-btn', { active: tmActive === t.key }]"
                  @click="tmActive = t.key"
                >{{ t.label }}</button>
              </div>
            </div>
            <div class="chart-wrap chart-expand">
              <svg viewBox="0 0 800 180" class="chart-svg">
                <defs>
                  <linearGradient id="areaUp" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#34c759" stop-opacity="0.3" />
                    <stop offset="100%" stop-color="#34c759" stop-opacity="0" />
                  </linearGradient>
                  <linearGradient id="areaDn" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#409eff" stop-opacity="0.3" />
                    <stop offset="100%" stop-color="#409eff" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <line v-for="i in 4" :key="'g'+i" x1="0" :y1="15+i*38" x2="800" :y2="15+i*38" stroke="#21262d" stroke-width="1" />
                <path :d="areaPath" :fill="assetChange >= 0 ? 'url(#areaUp)' : 'url(#areaDn)'" />
                <path :d="linePath" :stroke="assetChange >= 0 ? '#34c759' : '#409eff'" stroke-width="2" fill="none" stroke-linejoin="round" />
                <g v-for="(pt, i) in trendPts" :key="'a'+i">
                  <circle v-if="pt.tag" :cx="pt.x" :cy="pt.y" r="4" :fill="pt.tag === 'buy' ? '#34c759' : '#f56c6c'" stroke="#0d1117" stroke-width="2" />
                  <text v-if="pt.tag" :x="pt.x" :y="pt.y - 12" text-anchor="middle" fill="#8b949e" font-size="9">{{ pt.tag === 'buy' ? '买入' : '卖出' }}</text>
                </g>
                <text v-for="(l, i) in xLbls" :key="'xl'+i" :x="l.x" y="176" text-anchor="middle" fill="#484f58" font-size="9">{{ l.text }}</text>
              </svg>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：账户余额走势 -->
        <el-col :xs="24" :md="8">
          <el-card class="db-card" shadow="never">
            <div class="trend-header">
              <div>
                <div class="trend-label">账户余额</div>
                <div class="trend-value" style="color: #e6a23c;">¥{{ formatNumber(balanceValue) }}</div>
                <div class="trend-sub">
                  <span :class="balanceChange >= 0 ? 'rise' : 'fall'">
                    <el-icon :size="12"><Top v-if="balanceChange >= 0" /><Bottom v-else /></el-icon>
                    {{ Math.abs(balanceChange).toFixed(2) }}%
                  </span>
                  <span class="sub-muted">本期变化</span>
                </div>
              </div>
              <div class="time-filters">
                <button
                  v-for="t in balanceRanges" :key="t.key"
                  :class="['tm-btn', { active: balActive === t.key }]"
                  @click="balActive = t.key"
                >{{ t.label }}</button>
              </div>
            </div>
            <div class="chart-wrap chart-expand">
              <svg viewBox="0 0 340 180" class="chart-svg">
                <defs>
                  <linearGradient id="balGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#e6a23c" stop-opacity="0.25" />
                    <stop offset="100%" stop-color="#e6a23c" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <line v-for="i in 3" :key="'bg'+i" x1="0" :y1="20+i*48" x2="340" :y2="20+i*48" stroke="#21262d" stroke-width="1" />
                <path :d="balAreaPath" fill="url(#balGrad)" />
                <path :d="balLinePath" stroke="#e6a23c" stroke-width="2" fill="none" stroke-linejoin="round" />
                <g v-for="(pt, i) in balPts" :key="'ba'+i">
                  <circle v-if="pt.tag" :cx="pt.x" :cy="pt.y" r="4" :fill="pt.tag === 'deposit' ? '#67c23a' : '#f56c6c'" stroke="#0d1117" stroke-width="2" />
                  <text v-if="pt.tag" :x="pt.x" :y="pt.y - 10" text-anchor="middle" fill="#8b949e" font-size="8">{{ pt.tag === 'deposit' ? '入金' : '出金' }}</text>
                </g>
                <text v-for="(l, i) in balXLbls" :key="'bxl'+i" :x="l.x" y="175" text-anchor="middle" fill="#484f58" font-size="8">{{ l.text }}</text>
              </svg>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- ==================== 产品涨跌卡片 ==================== -->
      <el-row :gutter="8" class="db-section">
        <el-col
          v-for="p in miniProducts"
          :key="p.productCode"
          :xs="8" :sm="6" :md="4" :lg="3"
        >
          <el-card class="db-card mini-card" shadow="never" :class="(p.riseFallRate || 0) >= 0 ? 'bdr-rise' : 'bdr-fall'">
            <div class="mc-name">{{ p.productName }}</div>
            <div class="mc-price" :class="(p.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">{{ formatPrice(p.price) }}</div>
            <div class="mc-chg" :class="(p.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
              <el-icon :size="10"><Top v-if="(p.riseFallRate || 0) >= 0" /><Bottom v-else /></el-icon>
              {{ formatRate(p.riseFallRate) }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- ==================== 走势图 + 行情列表 ==================== -->
      <el-row :gutter="10" class="db-section eq-height-row">
        <el-col :xs="24" :md="14">
          <el-card class="db-card" shadow="never">
            <template #header>
              <div class="card-header-row">
                <span class="sect-title">行情走势</span>
                <el-radio-group v-model="symbolSel" size="small" class="symb-group">
                  <el-radio-button
                    v-for="p in topProds" :key="p.productCode" :value="p.productCode"
                  >{{ p.productName }}</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-wrap chart-expand">
              <svg viewBox="0 0 700 220" class="chart-svg">
                <line v-for="i in 5" :key="'kg'+i" x1="0" :y1="15+i*38" x2="700" :y2="15+i*38" stroke="#21262d" stroke-width="1" />
                <line v-for="i in 6" :key="'kv'+i" :x1="30+(i-1)*100" y1="15" :x2="30+(i-1)*100" y2="205" stroke="#21262d" stroke-width="1" stroke-dasharray="3,3" />
                <g v-for="(c, idx) in kPts" :key="idx" :transform="`translate(${c.x},0)`">
                  <line :x1="6" :y1="c.high" x2="6" :y2="c.low" :stroke="c.color" stroke-width="1.5" />
                  <rect :x="1" :y="Math.min(c.open,c.close)" width="10" :height="Math.max(Math.abs(c.close-c.open),1)" :fill="c.color" rx="1" />
                </g>
                <text v-for="(l, i) in priceLbls" :key="'kp'+i" x="695" :y="19+i*38" text-anchor="end" fill="#484f58" font-size="9">{{ l }}</text>
                <text v-for="(l, i) in dateLbls" :key="'kd'+i" :x="l.x" y="217" text-anchor="middle" fill="#484f58" font-size="9">{{ l.text }}</text>
              </svg>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="10">
          <el-card class="db-card fill-card" shadow="never">
            <template #header>
              <div class="card-header-row">
                <span class="sect-title">实时行情</span>
                <el-select v-model="mktFilter" size="small" placeholder="筛选" style="width:85px">
                  <el-option label="全部" value="" />
                  <el-option label="上涨" value="rise" />
                  <el-option label="下跌" value="fall" />
                </el-select>
              </div>
            </template>
            <div class="mkt-list">
              <div v-for="p in filteredList" :key="p.productCode" class="mkt-row">
                <div class="mkt-l">
                  <div class="mkt-name">{{ p.productName }}</div>
                  <div class="mkt-code">{{ p.productCode }}</div>
                </div>
                <div class="mkt-r">
                  <div class="mkt-price">¥{{ formatPrice(p.price) }}</div>
                  <div class="mkt-chg" :class="(p.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">{{ formatRate(p.riseFallRate) }}</div>
                </div>
              </div>
              <el-empty v-if="!filteredList.length" description="暂无数据" :image-size="60" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- ==================== 快捷入口 ==================== -->
      <el-card class="db-card ent-card" shadow="never">
        <template #header><span class="sect-title">快捷入口</span></template>
        <div class="ent-grid">
          <div v-for="e in entries" :key="e.label" class="ent-item" @click="$router.push(e.path)">
            <div class="ent-icon" :style="{ background: e.bg }"><el-icon :size="20"><component :is="e.icon" /></el-icon></div>
            <span class="ent-label">{{ e.label }}</span>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getProductList } from '@/api/product'
import { formatPrice, formatRate } from '@/utils/format'
import {
  Top, Bottom, Loading,
  User, Goods, DataLine, List, Message,
  Star, Reading, Search, Setting,
} from '@element-plus/icons-vue'
import type { FinProduct } from '@/types'

const loading = ref(true)
const products = ref<FinProduct[]>([])

// ============================
// 左侧：资产趋势
// ============================
const tmActive = ref('1D')

const timeRanges = [
  { key: '30m', label: '30m' },
  { key: '1H', label: '1H' },
  { key: '4H', label: '4H' },
  { key: '1D', label: '1D' },
]

const totalAsset = computed(() => {
  if (!products.value.length) return 0
  return products.value.reduce((s, p) => s + (p.price || 0) * (Math.floor(Math.random() * 100 + 20)), 0)
})

const assetChange = computed(() => (Math.random() - 0.35) * 10)

interface TrendPt { x: number; y: number; tag: 'buy' | 'sell' | null }

const trendPts = computed<TrendPt[]>(() => {
  const base = totalAsset.value || 1000000
  const n = 20
  const pts: TrendPt[] = []
  let v = base * 0.86
  for (let i = 0; i < n; i++) {
    v += (Math.random() - 0.46) * base * 0.018
    v *= assetChange.value >= 0 ? 1.0035 : 0.997
    const x = 40 + (i / (n - 1)) * 720
    const y = 162 - ((v - base * 0.78) / (base * 0.44)) * 130
    const tag = i === 5 ? 'buy' : i === 13 ? 'sell' : i === 17 ? 'buy' : null
    pts.push({ x, y, tag })
  }
  return pts
})

const linePath = computed(() =>
  trendPts.value.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
)
const areaPath = computed(() => {
  if (!trendPts.value.length) return ''
  const top = trendPts.value.map(p => `${p.x},${p.y}`).join(' ')
  return `M${trendPts.value[0].x},165 L${top} L${trendPts.value[trendPts.value.length - 1].x},165 Z`
})

const xLbls = computed(() => [0, 6, 12, 18].map(h => ({
  x: 40 + (h / 23) * 720,
  text: `${String(h).padStart(2, '0')}:00`,
})))

// ============================
// 右侧：账户余额走势
// ============================
const balActive = ref('1D')

const balanceRanges = [
  { key: '30m', label: '30m' },
  { key: '1H', label: '1H' },
  { key: '4H', label: '4H' },
  { key: '1D', label: '1D' },
  { key: '1W', label: '1W' },
  { key: '1M', label: '1M' },
]

interface BalPt { x: number; y: number; tag: 'deposit' | 'withdraw' | null }

const balanceValue = computed(() => {
  if (!products.value.length) return 0
  return products.value.reduce((s, p) => s + (p.price || 0) * Math.floor(Math.random() * 30 + 5), 0)
})

const balanceChange = computed(() => (Math.random() - 0.4) * 6)

const balPts = computed<BalPt[]>(() => {
  const base = balanceValue.value || 500000
  const n = 14
  const pts: BalPt[] = []
  let v = base * 0.9
  for (let i = 0; i < n; i++) {
    v += (Math.random() - 0.44) * base * 0.015
    v *= balanceChange.value >= 0 ? 1.004 : 0.998
    const x = 20 + (i / (n - 1)) * 300
    const y = 160 - ((v - base * 0.82) / (base * 0.36)) * 120
    const tag = i === 3 ? 'deposit' : i === 8 ? 'withdraw' : i === 12 ? 'deposit' : null
    pts.push({ x, y, tag })
  }
  return pts
})

const balLinePath = computed(() =>
  balPts.value.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
)

const balAreaPath = computed(() => {
  if (!balPts.value.length) return ''
  const top = balPts.value.map(p => `${p.x},${p.y}`).join(' ')
  return `M${balPts.value[0].x},165 L${top} L${balPts.value[balPts.value.length - 1].x},165 Z`
})

const balXLbls = computed(() =>
  [0, 4, 8, 12].map(i => ({
    x: 20 + (i / 13) * 300,
    text: [`12:00`, `14:00`, `16:00`, `18:00`][i],
  }))
)

// ============================
// 公共
// ============================
function formatNumber(v: number): string {
  if (v >= 1e8) return (v / 1e8).toFixed(2) + '亿'
  if (v >= 1e4) return (v / 1e4).toFixed(2) + '万'
  return v.toFixed(2)
}

// ---- 产品卡片 ----
const topProds = computed(() => products.value.slice(0, 5))
const miniProducts = computed(() => products.value.slice(0, 12))

// ---- K 线 SVG ----
interface KPt { x: number; open: number; close: number; high: number; low: number; color: string }
const symbolSel = ref('')
const curSym = computed(() => products.value.find(p => p.productCode === symbolSel.value))

const kPts = computed<KPt[]>(() => {
  const bp = curSym.value?.price || 100
  const n = 20
  const raw: { o: number; c: number; h: number; l: number }[] = []
  let p = bp * 0.9
  let mn = p, mx = p
  for (let i = 0; i < n; i++) {
    const o = p
    const c = o + (Math.random() - 0.48) * bp * 0.04
    const h = Math.max(o, c) + Math.random() * bp * 0.012
    const l = Math.min(o, c) - Math.random() * bp * 0.012
    mn = Math.min(mn, l); mx = Math.max(mx, h)
    raw.push({ o, c, h, l })
    p = c
  }
  const rng = mx - mn || 1
  return raw.map((r, i) => ({
    x: 40 + i * 30,
    open: 200 - ((r.o - mn) / rng) * 170,
    close: 200 - ((r.c - mn) / rng) * 170,
    high: 200 - ((r.h - mn) / rng) * 170,
    low: 200 - ((r.l - mn) / rng) * 170,
    color: r.c >= r.o ? '#67c23a' : '#f56c6c',
  }))
})

const priceLbls = computed(() => {
  const bp = curSym.value?.price || 100
  const mn = bp * 0.82, mx = bp * 1.18
  return [0, 1, 2, 3, 4].map(i => (mx - (mx - mn) / 4 * i).toFixed(2))
})

const dateLbls = computed(() =>
  [0, 5, 10, 15, 19].map(i => ({ x: 40 + i * 30, text: `${String(9 + i).padStart(2, '0')}:30` }))
)

// ---- 行情过滤 ----
const mktFilter = ref('')
const filteredList = computed(() => {
  if (!mktFilter.value) return products.value
  return products.value.filter(p => {
    const r = p.riseFallRate || 0
    return mktFilter.value === 'rise' ? r > 0 : r < 0
  })
})

// ---- 快捷入口 ----
const entries = [
  { label: '用户管理', path: '/user', icon: User, bg: '#409eff' },
  { label: '产品管理', path: '/product', icon: Goods, bg: '#67c23a' },
  { label: '行情数据', path: '/market', icon: DataLine, bg: '#e6a23c' },
  { label: '交易管理', path: '/trade', icon: List, bg: '#f56c6c' },
  { label: '自选管理', path: '/favorite', icon: Star, bg: '#d29922' },
  { label: '消息管理', path: '/message', icon: Message, bg: '#00b894' },
  { label: '管理员', path: '/system/admin', icon: Setting, bg: '#8e44ad' },
  { label: 'ES 搜索', path: '/search', icon: Search, bg: '#1a6dff' },
]

// ---- 加载数据 ----
async function fetchAll() {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = (res.data || []) as FinProduct[]
    if (products.value.length && !symbolSel.value) symbolSel.value = products.value[0].productCode
  } catch { /* ignore */ }
  finally { loading.value = false }
}

onMounted(fetchAll)
</script>

<style>
/* ============================================
   深色金融仪表盘 — 通过 .dark-dashboard 隔离
   ============================================ */
.dark-dashboard {
  --dbbg: #0d1117;
  --dbcard: #161b22;
  --dbbrd: #21262d;
  --dbtxt: #e6edf3;
  --dbsec: #8b949e;
  --dbdim: #484f58;
  --dbgreen: #67c23a;
  --dbred: #f56c6c;
  --dbblue: #409eff;

  max-width: 1280px;
}
.db-loading {
  display: flex; flex-direction: column; align-items: center;
  padding: 100px 0; gap: 14px; color: var(--dbsec); font-size: 14px;
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.db-section { margin-bottom: 8px; }

/* ===== Equal-height flex rows ===== */
.dark-dashboard .eq-height-row { display: flex; flex-wrap: wrap; }
.dark-dashboard .eq-height-row > .el-col { display: flex; flex-direction: column; }
.dark-dashboard .eq-height-row .db-card { flex: 1; display: flex; flex-direction: column; }
.dark-dashboard .eq-height-row .db-card .el-card__body {
  flex: 1; display: flex; flex-direction: column;
}
.dark-dashboard .eq-height-row .chart-expand { flex: 1; display: flex; align-items: center; }
.dark-dashboard .eq-height-row .chart-expand .chart-svg {
  width: 100%; height: auto; max-height: 100%;
}
.dark-dashboard .eq-height-row .fill-card .mkt-list {
  flex: 1; max-height: none; overflow-y: auto;
}

/* ===== Card ===== */
.dark-dashboard .el-card.db-card {
  background: var(--dbcard) !important;
  border: 1px solid var(--dbbrd) !important;
  border-radius: 10px !important;
  margin-bottom: 8px;
  transition: border-color 0.2s;
}
.dark-dashboard .el-card.db-card:hover {
  border-color: #30363d !important;
  box-shadow: none !important;
}
.dark-dashboard .el-card__header {
  padding: 10px 16px !important;
  border-bottom: 1px solid var(--dbbrd) !important;
  color: var(--dbtxt); font-size: 13px; font-weight: 600;
}

/* ===== Trend ===== */
.trend-header { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
.trend-label { font-size: 12px; color: var(--dbsec); margin-bottom: 2px; }
.trend-value {
  font-size: 22px; font-weight: 700; color: var(--dbtxt);
  font-family: 'Courier New', monospace; line-height: 1.2;
}
.trend-sub { display: flex; align-items: center; gap: 8px; margin-top: 4px; font-size: 12px; }
.sub-muted { color: var(--dbdim); }
.time-filters { display: flex; gap: 4px; flex-wrap: wrap; }
.tm-btn {
  background: transparent; border: 1px solid var(--dbbrd); color: var(--dbsec);
  padding: 3px 10px; border-radius: 5px; font-size: 11px; cursor: pointer; transition: all 0.15s;
}
.tm-btn:hover { border-color: var(--dbblue); color: var(--dbblue); }
.tm-btn.active { background: var(--dbblue); border-color: var(--dbblue); color: #fff; }
.chart-wrap { margin-top: 8px; }
.chart-svg { width: 100%; height: auto; display: block; }

.rise { color: var(--dbgreen) !important; }
.fall { color: var(--dbred) !important; }

/* ===== Mini Cards ===== */
.mini-card.bdr-rise { border-left: 3px solid var(--dbgreen) !important; }
.mini-card.bdr-fall { border-left: 3px solid var(--dbred) !important; }
.mc-name { font-size: 12px; font-weight: 600; color: var(--dbtxt); }
.mc-price { font-size: 16px; font-weight: 700; margin-top: 6px; font-family: 'Courier New', monospace; }
.mc-chg { font-size: 11px; font-weight: 600; margin-top: 3px; display: flex; align-items: center; gap: 2px; }

/* ===== Section title ===== */
.sect-title { font-size: 13px; font-weight: 600; color: var(--dbtxt); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 6px; }

/* ===== Market List ===== */
.mkt-list { max-height: 410px; overflow-y: auto; }
.mkt-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 0; border-bottom: 1px solid var(--dbbrd); transition: background 0.15s;
}
.mkt-row:last-child { border-bottom: none; }
.mkt-row:hover { background: rgba(255,255,255,0.03); }
.mkt-l { display: flex; flex-direction: column; gap: 1px; }
.mkt-name { font-size: 12px; font-weight: 600; color: var(--dbtxt); }
.mkt-code { font-size: 10px; color: var(--dbdim); }
.mkt-r { text-align: right; }
.mkt-price { font-size: 13px; font-weight: 600; color: var(--dbtxt); font-family: 'Courier New', monospace; }
.mkt-chg { font-size: 11px; font-weight: 600; margin-top: 1px; }
.mkt-list::-webkit-scrollbar { width: 4px; }
.mkt-list::-webkit-scrollbar-thumb { background: var(--dbbrd); border-radius: 2px; }

/* ===== Entries ===== */
.ent-card .ent-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); gap: 8px; }
.ent-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 14px 6px; border-radius: 8px; cursor: pointer; transition: background 0.15s;
}
.ent-item:hover { background: rgba(255,255,255,0.05); }
.ent-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.ent-label { font-size: 11px; color: var(--dbsec); white-space: nowrap; }

/* ===== Element Plus overrides ===== */
.dark-dashboard .symb-group .el-radio-button__inner {
  background: transparent; border-color: var(--dbbrd); color: var(--dbsec);
  font-size: 11px; padding: 4px 10px;
}
.dark-dashboard .symb-group .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: var(--dbblue); border-color: var(--dbblue); color: #fff; box-shadow: none;
}
.dark-dashboard .symb-group .el-radio-button:not(:first-child) .el-radio-button__inner {
  border-left-color: var(--dbbrd);
}
.dark-dashboard .el-select .el-input__wrapper {
  background: transparent; box-shadow: 0 0 0 1px var(--dbbrd) inset;
}
.dark-dashboard .el-select .el-input__inner { color: var(--dbtxt); }
.dark-dashboard .el-empty__description p { color: var(--dbdim); }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .trend-header { flex-direction: column; }
  .trend-value { font-size: 18px; }
  .card-header-row { flex-direction: column; align-items: flex-start; }
  .ent-grid { grid-template-columns: repeat(4, 1fr) !important; }
}
</style>
