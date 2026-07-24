<template>
  <ErrorBoundary>
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <Suspense>
          <!-- B1: KeepAlive 缓存由路由 meta.keepAlive 控制（默认 true）
               cached pages → 稳定 key（group || path）→ 复用缓存实例
               non-cached pages → 递增 nonce key → 组件创建销毁，表单不保留 -->
          <KeepAlive :max="10">
            <component :is="Component" :key="routeKey" />
          </KeepAlive>
          <template #fallback>
            <PageLoading />
          </template>
        </Suspense>
      </transition>
    </router-view>
    <FloatingActions />
  </ErrorBoundary>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import FloatingActions from '@/components/FloatingActions.vue'
import PageLoading from '@/components/PageLoading.vue'

const route = useRoute()

// B1: 基于路由 meta 的动态缓存 key
// keepAlive !== false → 稳定 key（group || path）→ 缓存命中
// keepAlive === false → 递增 nonce → 组件每次重建
let nonceCounter = 0
const routeKey = ref('')

watch(() => route.path, () => {
  if (route.meta.keepAlive === false) {
    nonceCounter++
    routeKey.value = route.fullPath + '__nc_' + nonceCounter
  } else {
    routeKey.value = (route.meta.group as string) || route.path
  }
}, { immediate: true })

// S2: 全局 token 过期定时检测（每 5 分钟），无路由跳转时主动登出
const TOKEN_CHECK_INTERVAL = 5 * 60 * 1000
let tokenTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  tokenTimer = setInterval(() => {
    const userStore = useUserStore()
    const router = useRouter()
    if (userStore.isLoggedIn && userStore.checkTokenExpired()) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/auth/login')
    }
  }, TOKEN_CHECK_INTERVAL)
})

onUnmounted(() => {
  if (tokenTimer) {
    clearInterval(tokenTimer)
    tokenTimer = null
  }
})
</script>

<style>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
