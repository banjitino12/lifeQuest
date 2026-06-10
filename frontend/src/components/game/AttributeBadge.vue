<script setup lang="ts">
import type { AttributeViewModel } from '@/types';

defineProps<{
  attribute: AttributeViewModel;
}>();
</script>

<template>
  <div class="attribute-badge">
    <span class="attribute-label">{{ attribute.label }}</span>
    <strong>{{ attribute.value }}</strong>
    <span
      v-if="attribute.change !== undefined"
      class="attribute-change"
      :class="{ 'attribute-change-negative': attribute.change < 0 }"
    >
      {{ attribute.change > 0 ? '+' : '' }}{{ attribute.change }}
    </span>
  </div>
</template>

