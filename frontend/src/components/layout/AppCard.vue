<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string;
    subtitle?: string;
  }>(),
  {
    title: '',
    subtitle: '',
  },
);
</script>

<template>
  <article class="app-card">
    <header v-if="title || subtitle || $slots.actions" class="app-card-header">
      <div>
        <h3 v-if="title">{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="app-card-actions">
        <slot name="actions" />
      </div>
    </header>
    <slot />
  </article>
</template>

