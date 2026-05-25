<template>
  <div class="landing-page">
    <!-- Navbar -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="nav-left">
          <span class="logo">理财服务平台</span>
        </div>
        <div class="nav-right">
          <template v-if="userStore.token">
            <span class="user-info">{{ userStore.nickname || userStore.username }}</span>
            <el-button text @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="hero">
      <h1>智慧投资 · 稳健增值</h1>
      <p>专业理财服务平台，为您提供全方位的投资解决方案</p>
    </section>

    <!-- Product List -->
    <section class="section">
      <h2 class="section-title">产品中心</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="p in products" :key="p.id" class="product-card">
          <el-card shadow="hover">
            <h3>{{ p.productName }}</h3>
            <p class="price">¥{{ p.price }}</p>
            <p class="code">{{ p.productCode }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- Market Data -->
    <section class="section market-section">
      <h2 class="section-title">实时行情</h2>
      <el-table :data="marketData" stripe style="width: 100%">
        <el-table-column prop="productCode" label="产品代码" width="120" />
        <el-table-column prop="currentPrice" label="当前价" width="120" />
        <el-table-column label="涨跌幅" width="120">
          <template #default="{ row }">
            <span :style="{ color: (row.riseFallRate || 0) >= 0 ? '#f56c6c' : '#67c23a' }">
              {{ row.riseFallRate ?? '-' }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="highestPrice" label="最高" width="100" />
        <el-table-column prop="lowestPrice" label="最低" width="100" />
        <el-table-column prop="marketTime" label="时间" />
      </el-table>
    </section>

    <!-- News -->
    <section class="section">
      <h2 class="section-title">财经资讯</h2>
      <div v-for="n in news" :key="n.id" class="news-item">
        <h3>{{ n.title }}</h3>
        <p class="news-meta">{{ n.source }} · {{ n.createTime }}</p>
        <p class="news-summary">{{ n.summary || n.content?.substring(0, 120) }}</p>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>© 2026 理财服务平台. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/index'

const router = useRouter()
const userStore = useUserStore()

const products = ref<any[]>([])
const marketData = ref<any[]>([])
const news = ref<any[]>([])

async function fetchProducts() {
  const res = await request.get('/product/wea-product/page', { params: { pageNum: 1, pageSize: 4 } })
  products.value = res.data?.records ?? []
}

async function fetchMarketData() {
  const res = await request.get('/product/wea-market-data')
  marketData.value = res.data ?? []
}

async function fetchNews() {
  const res = await request.get('/message/wea-news/page', { params: { pageNum: 1, pageSize: 5 } })
  news.value = res.data?.records ?? []
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出')
}

onMounted(() => {
  fetchProducts()
  fetchMarketData()
  fetchNews()
})
</script>

<style scoped>
.landing-page { min-height: 100vh; background: #f5f7fa; }
.navbar { background: #1a365d; color: #fff; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.nav-inner { display: flex; justify-content: space-between; align-items: center; height: 56px; max-width: 1200px; margin: 0 auto; }
.logo { font-size: 18px; font-weight: 700; letter-spacing: 2px; }
.user-info { margin-right: 12px; font-size: 14px; }
.hero { text-align: center; padding: 60px 20px; background: linear-gradient(135deg, #1a365d, #2d5a8e); color: #fff; }
.hero h1 { font-size: 36px; margin-bottom: 12px; }
.hero p { font-size: 16px; opacity: 0.85; }
.section { max-width: 1200px; margin: 32px auto; padding: 0 20px; }
.section-title { font-size: 22px; margin-bottom: 20px; color: #1a365d; }
.product-card { margin-bottom: 16px; }
.price { font-size: 24px; font-weight: 700; color: #e6a23c; }
.code { font-size: 12px; color: #909399; }
.news-item { padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.news-item h3 { font-size: 16px; margin-bottom: 6px; }
.news-meta { font-size: 12px; color: #909399; margin-bottom: 8px; }
.news-summary { font-size: 14px; color: #606266; line-height: 1.6; }
.footer { text-align: center; padding: 24px; color: #909399; font-size: 12px; }
</style>
