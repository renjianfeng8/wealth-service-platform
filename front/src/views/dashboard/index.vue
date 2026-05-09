<template>
  <div class="dashboard">
    <h2 style="margin-bottom:20px;">控制台</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>系统功能模块</span></template>
          <el-table :data="modules" stripe>
            <el-table-column prop="name" label="模块" />
            <el-table-column prop="desc" label="功能说明" />
            <el-table-column prop="path" label="路由">
              <template #default="{ row }">
                <el-button type="primary" link @click="$router.push(row.path)">进入</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>项目信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="技术栈">SpringBoot 3.3.5 + Cloud 2023.0.3</el-descriptions-item>
            <el-descriptions-item label="JDK 版本">21.0.3</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0.37</el-descriptions-item>
            <el-descriptions-item label="搜索">Elasticsearch 8.11.0</el-descriptions-item>
            <el-descriptions-item label="消息队列">RabbitMQ 3.10.20</el-descriptions-item>
            <el-descriptions-item label="前端">Vue 3 + Element Plus</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
const stats = [
  { label: '用户数', value: 128 },
  { label: '产品数', value: 56 },
  { label: '今日交易', value: 234 },
  { label: '消息数', value: 89 },
]

const modules = [
  { name: '用户管理', desc: '系统用户注册、登录、信息管理', path: '/user' },
  { name: '产品管理', desc: '金融产品上架、编辑、分类管理', path: '/product' },
  { name: '行情数据', desc: '实时行情、历史行情数据管理', path: '/market' },
  { name: '交易管理', desc: '委托单管理、交易查询', path: '/trade' },
  { name: '自选管理', desc: '用户自选产品管理', path: '/favorite' },
  { name: '站内消息', desc: '消息推送、通知管理', path: '/message' },
  { name: '管理员管理', desc: '后台管理员、角色、权限管理', path: '/system/admin' },
  { name: 'ES 搜索', desc: '基于 Elasticsearch 的产品搜索', path: '/search' },
]
</script>

<style scoped>
.dashboard { padding: 0; }

.stat-card { text-align: center; cursor: pointer; border-radius: var(--radius) !important; transition: var(--transition); }
.stat-card:hover { transform: translateY(-4px); }
.stat-value { font-size: 32px; font-weight: 700; background: linear-gradient(135deg, var(--primary), var(--primary-dark)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; line-height: 1.3; }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 8px; letter-spacing: 0.5px; }

.dashboard-section-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
</style>
