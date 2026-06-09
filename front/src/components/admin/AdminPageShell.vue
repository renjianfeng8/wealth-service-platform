<template>
  <section class="admin-page-shell">
    <header class="admin-page-shell__header">
      <div>
        <h2>{{ title }}</h2>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.toolbar" class="admin-page-shell__toolbar">
        <slot name="toolbar" />
      </div>
    </header>

    <slot />
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.admin-page-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-page-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 0 2px;
}

.admin-page-shell__header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 22px;
  font-weight: 650;
  line-height: 1.3;
}

.admin-page-shell__header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.admin-page-shell__toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .admin-page-shell__header {
    flex-direction: column;
  }
}
</style>
