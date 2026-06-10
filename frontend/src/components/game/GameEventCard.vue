<script setup lang="ts">
import type { GameEventViewModel } from '@/types';

defineProps<{
  event: GameEventViewModel;
}>();
</script>

<template>
  <article class="game-event-card" :data-event-type="event.type.toLowerCase()">
    <div class="game-event-icon" aria-hidden="true">
      {{ event.type === 'BUFF' ? '+' : event.type === 'DEBUFF' ? '-' : '!' }}
    </div>
    <div class="game-event-content">
      <h4>{{ event.title }}</h4>
      <p v-if="event.description">{{ event.description }}</p>
      <span v-if="event.effectText">{{ event.effectText }}</span>
    </div>
    <time v-if="event.remainingText" class="game-event-time">{{ event.remainingText }}</time>
  </article>
</template>

