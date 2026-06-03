<template>
  <ErrorBoundary>
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <Suspense>
          <component :is="Component" :key="$route.meta.group || $route.path" />
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
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import FloatingActions from '@/components/FloatingActions.vue'
import PageLoading from '@/components/PageLoading.vue'
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
