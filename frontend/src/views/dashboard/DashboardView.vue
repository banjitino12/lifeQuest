<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { dailyLogApi } from '@/api';
import { AttributeRadarChart, ChartPanel } from '@/components/charts';
import { AttributeBadge, GameEventCard } from '@/components/game';
import { AppCard, AppPage } from '@/components/layout';
import { useAuthStore } from '@/stores';
import type { AttributeViewModel, GameEventViewModel, SettlementResponse } from '@/types';

const router = useRouter();
const authStore = useAuthStore();
const today = new Date().toLocaleDateString('en-CA');
const todaySettlement = ref<SettlementResponse | null>(null);
const loading = ref(false);

const dashboardUser = computed(() => authStore.currentUser?.username ?? '冒险者');

const summary = computed(() => ({
  score: todaySettlement.value?.score.dailyScore ?? 8.2,
  rating: todaySettlement.value?.score.rating ?? 'B+',
  level: 18,
  exp: 4260,
  nextExp: 6480,
  streakDays: 23,
  routeName: '后端实习路线',
  routeStage: 'Redis 进阶',
}));

const attributes = computed<AttributeViewModel[]>(() => {
  const change = todaySettlement.value?.attributeChange;
  return [
    { key: 'focus', label: '专注力', value: 72, change: change?.focusDelta },
    { key: 'discipline', label: '自律', value: 68, change: change?.disciplineDelta },
    { key: 'knowledge', label: '知识积累', value: 76, change: change?.knowledgeDelta },
    { key: 'energy', label: '精力', value: 62, change: change?.energyDelta },
    { key: 'mood', label: '情绪稳定', value: 70, change: change?.moodDelta },
    { key: 'execution', label: '执行力', value: 74, change: change?.executionDelta },
    { key: 'balance', label: '生活平衡', value: 66, change: change?.balanceDelta },
  ];
});

const events = computed<GameEventViewModel[]>(() => {
  if (todaySettlement.value?.events.length) {
    return todaySettlement.value.events.map((event) => ({
      id: event.eventCode,
      title: `${event.eventName} Lv.${event.eventLevel}`,
      description: event.eventDescription,
      type: event.eventType,
      effectText: event.effectJson,
    }));
  }

  return [
    {
      id: 'focus-placeholder',
      title: '专注时光 Lv.1',
      description: '完成今日记录后会展示真实 Buff / Debuff。',
      type: 'BUFF',
      effectText: '等待今日结算',
    },
  ];
});

const quickActions: Array<{ label: string; routeName: string; type: 'primary' | 'default' }> = [
  { label: '开始今日记录', routeName: 'daily-log', type: 'primary' },
  { label: '查看今日结算', routeName: 'settlement', type: 'default' },
  { label: '成长路线', routeName: 'route', type: 'default' },
];

async function loadTodaySettlement() {
  loading.value = true;
  try {
    todaySettlement.value = await dailyLogApi.getSettlementByDate(today);
  } catch {
    todaySettlement.value = null;
  } finally {
    loading.value = false;
  }
}

function goAction(routeName: string) {
  if (routeName === 'settlement') {
    router.push({ name: routeName, query: { date: today } });
    return;
  }
  router.push({ name: routeName });
}

onMounted(() => {
  if (authStore.isAuthenticated && !authStore.currentUser) {
    void authStore.fetchCurrentUser();
  }
  void loadTodaySettlement();
});
</script>

<template>
  <AppPage
    title="成长首页"
    eyebrow="Dashboard"
    :description="`欢迎回来，${dashboardUser}。今天也把行动变成经验值。`"
  >
    <div class="dashboard-summary">
      <AppCard title="今日评分" subtitle="来自今日结算或占位数据">
        <div class="dashboard-score">
          <strong>{{ summary.score }}</strong>
          <span>{{ summary.rating }}</span>
        </div>
      </AppCard>

      <AppCard title="当前等级" :subtitle="`EXP ${summary.exp} / ${summary.nextExp}`">
        <div class="dashboard-level">
          <strong>Lv.{{ summary.level }}</strong>
          <el-progress :percentage="Math.round((summary.exp / summary.nextExp) * 100)" :stroke-width="10" />
        </div>
      </AppCard>

      <AppCard title="当前路线" :subtitle="summary.routeStage">
        <p class="dashboard-primary-text">{{ summary.routeName }}</p>
      </AppCard>

      <AppCard title="连续记录">
        <p class="dashboard-primary-text">{{ summary.streakDays }} 天</p>
      </AppCard>
    </div>

    <div class="dashboard-actions">
      <el-button
        v-for="action in quickActions"
        :key="action.label"
        :type="action.type"
        @click="goAction(action.routeName)"
      >
        {{ action.label }}
      </el-button>
    </div>

    <div class="dashboard-grid">
      <ChartPanel title="核心属性雷达图" :state="loading ? 'loading' : 'ready'">
        <AttributeRadarChart :attributes="attributes" />
      </ChartPanel>

      <AppCard title="属性快照" subtitle="后端聚合接口完成前使用基础展示">
        <div class="attribute-grid">
          <AttributeBadge v-for="attribute in attributes" :key="attribute.key" :attribute="attribute" />
        </div>
      </AppCard>

      <AppCard title="今日 Buff / Debuff">
        <div class="event-list">
          <GameEventCard v-for="event in events" :key="event.id" :event="event" />
        </div>
      </AppCard>

      <AppCard title="今日建议" :subtitle="todaySettlement?.llm.fallbackUsed ? '规则降级建议' : '基础建议'">
        <p class="settlement-text">
          {{ todaySettlement?.basicSuggestion ?? '完成今日记录后，这里会展示规则评分后的基础建议。' }}
        </p>
      </AppCard>
    </div>
  </AppPage>
</template>
