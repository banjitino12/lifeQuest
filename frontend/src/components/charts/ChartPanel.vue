<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    state?: 'ready' | 'empty' | 'loading';
    emptyText?: string;
  }>(),
  {
    emptyText: '暂无数据',
    state: 'ready',
  },
);
</script>

<template>
  <section class="chart-panel">
    <header class="chart-panel-header">
      <h3>{{ title }}</h3>
      <slot name="actions" />
    </header>
    <div class="chart-panel-body">
      <p v-if="state === 'empty'" class="chart-placeholder">{{ emptyText }}</p>
      <p v-else-if="state === 'loading'" class="chart-placeholder">加载中...</p>
      <slot v-else />
    </div>
  </section>
</template>

