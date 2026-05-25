import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export const useAppStore = defineStore('app', () => {
  const visitedViews = ref<RouteLocationNormalized[]>([])
  const cachedViews = ref<string[]>([])

  const visitedViewTitles = computed(() =>
    visitedViews.value.map(v => v.meta?.title as string || v.name as string || '')
  )

  function addView(view: RouteLocationNormalized) {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push(view)
    if (view.name) cachedViews.value.push(view.name as string)
  }

  function removeView(view: RouteLocationNormalized) {
    const i = visitedViews.value.findIndex(v => v.path === view.path)
    if (i > -1) visitedViews.value.splice(i, 1)
    if (view.name) {
      const j = cachedViews.value.indexOf(view.name as string)
      if (j > -1) cachedViews.value.splice(j, 1)
    }
  }

  function closeOtherViews(view: RouteLocationNormalized) {
    visitedViews.value = visitedViews.value.filter(v => v.path === view.path)
    if (view.name) {
      cachedViews.value = [view.name as string]
    }
  }

  function closeAllViews() {
    visitedViews.value = []
    cachedViews.value = []
  }

  return { visitedViews, cachedViews, visitedViewTitles, addView, removeView, closeOtherViews, closeAllViews }
})
