<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { dailyLogApi } from '@/api';
import { AppCard, AppPage } from '@/components/layout';
import { AttributeBadge, GameEventCard } from '@/components/game';
import type { AttributeViewModel, GameEventViewModel, SettlementResponse } from '@/types';
import { getApiErrorMessage } from '@/utils';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const settlement = ref<SettlementResponse | null>(null);

const selectedDate = computed(() => {
  const date = route.query.date;
  return typeof date === 'string' ? date : new Date().toLocaleDateString('en-CA');
});

const scoreItems = computed(() => {
  if (!settlement.value) {
    return [];
  }

  const score = settlement.value.score;
  return [
    { label: '学习成长', value: score.growthScore },
    { label: '任务执行', value: score.executionScore },
    { label: '精力恢复', value: score.energyScore },
    { label: '情绪状态', value: score.moodScore },
    { label: '娱乐控制', value: score.distractionScore },
    { label: '复盘质量', value: score.reflectionScore },
  ];
});

const attributeItems = computed<AttributeViewModel[]>(() => {
  const change = settlement.value?.attributeChange;
  if (!change) {
    return [];
  }

  return [
    { key: 'focus', label: '专注力', value: change.focusDelta, change: change.focusDelta },
    { key: 'discipline', label: '自律', value: change.disciplineDelta, change: change.disciplineDelta },
    { key: 'knowledge', label: '知识积累', value: change.knowledgeDelta, change: change.knowledgeDelta },
    { key: 'energy', label: '精力', value: change.energyDelta, change: change.energyDelta },
    { key: 'mood', label: '情绪稳定', value: change.moodDelta, change: change.moodDelta },
    { key: 'execution', label: '执行力', value: change.executionDelta, change: change.executionDelta },
    { key: 'balance', label: '生活平衡', value: change.balanceDelta, change: change.balanceDelta },
  ];
});

const eventItems = computed<GameEventViewModel[]>(() =>
  (settlement.value?.events ?? []).map((event) => ({
    id: event.eventCode,
    title: `${event.eventName} Lv.${event.eventLevel}`,
    description: event.eventDescription,
    type: event.eventType,
    effectText: event.effectJson,
  })),
);

async function loadSettlement() {
  loading.value = true;
  try {
    settlement.value = await dailyLogApi.getSettlementByDate(selectedDate.value);
  } catch (error) {
    settlement.value = null;
    ElMessage.warning(getApiErrorMessage(error, '还没有找到这一天的结算，请先提交每日记录'));
  } finally {
    loading.value = false;
  }
}

function goToDailyLog() {
  router.push({ name: 'daily-log' });
}

onMounted(loadSettlement);

watch(selectedDate, () => {
  void loadSettlement();
});
</script>

<template>
  <AppPage
    title="每日结算"
    eyebrow="Daily Settlement"
    description="查看规则评分、经验变化、属性变化、游戏化事件和基础建议。"
  >
    <template #actions>
      <el-date-picker
        :model-value="selectedDate"
        type="date"
        value-format="YYYY-MM-DD"
        @update:model-value="(value: string) => router.push({ name: 'settlement', query: { date: value } })"
      />
      <el-button :loading="loading" @click="loadSettlement">读取结算</el-button>
    </template>

    <el-empty v-if="!settlement && !loading" description="暂无结算结果">
      <el-button type="primary" @click="goToDailyLog">去提交每日记录</el-button>
    </el-empty>

    <div v-else v-loading="loading" class="settlement-layout">
      <AppCard title="今日评级" :subtitle="settlement?.logDate">
        <div class="settlement-score">
          <strong>{{ settlement?.score.dailyScore ?? '--' }}</strong>
          <span>{{ settlement?.score.rating ?? '--' }}</span>
          <p>经验变化 +{{ settlement?.attributeChange.expDelta ?? 0 }} XP</p>
        </div>
      </AppCard>

      <AppCard title="LLM 状态" :subtitle="settlement?.llm.fallbackUsed ? '已使用规则降级' : '反馈可用'">
        <p class="settlement-text">{{ settlement?.llm.feedback }}</p>
        <p class="settlement-muted">{{ settlement?.llm.storyNarration }}</p>
      </AppCard>

      <AppCard title="六维评分原因">
        <div class="score-list">
          <div v-for="item in scoreItems" :key="item.label" class="score-row">
            <span>{{ item.label }}</span>
            <el-progress :percentage="Number(item.value)" :stroke-width="10" />
          </div>
        </div>
      </AppCard>

      <AppCard title="属性变化" :subtitle="settlement?.attributeChange.reasons.focus">
        <div class="attribute-grid">
          <AttributeBadge v-for="attribute in attributeItems" :key="attribute.key" :attribute="attribute" />
        </div>
      </AppCard>

      <AppCard title="今日触发事件">
        <div v-if="eventItems.length" class="event-list">
          <GameEventCard v-for="event in eventItems" :key="event.id" :event="event" />
        </div>
        <el-empty v-else description="今天没有额外事件" />
      </AppCard>

      <AppCard title="基础建议">
        <p class="settlement-text">{{ settlement?.basicSuggestion }}</p>
      </AppCard>

      <AppCard title="明日任务" subtitle="完整任务生成将在 LLM / 任务模块完善后接入">
        <div v-if="settlement?.tomorrowTasks.length" class="task-list">
          <div v-for="task in settlement.tomorrowTasks" :key="task.title" class="task-item">
            <span>{{ task.title }}</span>
            <small>{{ task.taskType }} / {{ task.generatedBy }}</small>
          </div>
        </div>
        <el-empty v-else description="暂无明日任务，先展示规则结算结果" />
      </AppCard>
    </div>
  </AppPage>
</template>
