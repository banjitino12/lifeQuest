<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    eyebrow?: string;
    description?: string;
  }>(),
  {
    eyebrow: '',
    description: '',
  },
);
</script>

<template>
  <section class="app-page">
    <header class="app-page-header">
      <p v-if="eyebrow" class="app-page-eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p v-if="description" class="app-page-description">{{ description }}</p>
      <div v-if="$slots.actions" class="app-page-actions">
        <slot name="actions" />
      </div>
    </header>

    <div class="app-page-body">
      <slot />
    </div>
  </section>
</template>

