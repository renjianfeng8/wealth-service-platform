<template>
  <div class="tags-view-container">
    <el-tabs
      v-model="activeTab"
      type="card"
      closable
      @tab-remove="removeTab"
      @tab-click="switchTab"
      @contextmenu.prevent="handleContextMenu"
    >
      <el-tab-pane
        v-for="view in visitedViews"
        :key="view.path"
        :label="view.meta?.title as string || '未命名'"
        :name="view.path"
      />
    </el-tabs>
    <ul v-if="menuVisible" class="tags-view-menu" :style="menuStyle">
      <li @click="closeCurrent">关闭当前</li>
      <li @click="closeOthers">关闭其他</li>
      <li @click="closeAll">关闭全部</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import type { RouteLocationNormalized } from 'vue-router'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const visitedViews = computed(() => appStore.visitedViews)
const activeTab = ref('')
const menuVisible = ref(false)
const menuStyle = ref({ left: '0px', top: '0px' })

watch(route, (to) => {
  appStore.addView(to)
  activeTab.value = to.path
}, { immediate: true })

function switchTab(tab: any) {
  const path = tab.props.name
  router.push(path)
}

function removeTab(path: string) {
  const view = visitedViews.value.find(v => v.path === path)
  if (!view) return
  appStore.removeView(view)
  if (activeTab.value === path) {
    const last = visitedViews.value[visitedViews.value.length - 1]
    if (last) router.push(last.path)
  }
}

function handleContextMenu(e: MouseEvent) {
  const tabEl = (e.target as HTMLElement).closest('.el-tabs__item')
  if (!tabEl) return
  menuStyle.value = { left: `${e.clientX}px`, top: `${e.clientY}px` }
  menuVisible.value = true
  const hide = () => { menuVisible.value = false; document.removeEventListener('click', hide) }
  document.addEventListener('click', hide)
}

function closeCurrent() {
  removeTab(activeTab.value)
}

function closeOthers() {
  const current = visitedViews.value.find(v => v.path === activeTab.value)
  if (current) appStore.closeOtherViews(current)
}

function closeAll() {
  appStore.closeAllViews()
  router.push('/dashboard')
  menuVisible.value = false
}
</script>

<style scoped>
.tags-view-container {
  background: #fff;
  border-bottom: 1px solid var(--fl-border);
  padding: 4px 8px 0;
  position: relative;
}
.tags-view-container :deep(.el-tabs__header) {
  margin: 0;
}
.tags-view-container :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}
.tags-view-container :deep(.el-tabs__item) {
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  border: 1px solid var(--fl-border);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  margin-right: 4px;
  padding: 0 14px;
}
.tags-view-container :deep(.el-tabs__item.is-active) {
  background: var(--fl-content-bg);
  border-bottom-color: var(--fl-content-bg);
  color: var(--fl-primary);
}
.tags-view-menu {
  position: fixed;
  background: #fff;
  border: 1px solid var(--fl-border);
  border-radius: 6px;
  box-shadow: var(--fl-shadow-hover);
  list-style: none;
  margin: 0;
  padding: 4px 0;
  z-index: 3000;
  min-width: 120px;
}
.tags-view-menu li {
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  color: var(--fl-text-secondary);
}
.tags-view-menu li:hover {
  background: var(--fl-primary-light);
  color: var(--fl-primary);
}
</style>
