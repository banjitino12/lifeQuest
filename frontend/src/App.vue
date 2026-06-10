<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { RouterLink, RouterView, useRouter } from 'vue-router';

import { useAuthStore } from '@/stores';

const navigationItems = [
  { label: '首页', to: '/dashboard' },
  { label: '每日记录', to: '/daily-log' },
  { label: '结算', to: '/settlement' },
  { label: '路线', to: '/route' },
  { label: '趋势', to: '/trends' },
  { label: '周报', to: '/weekly' },
];

const router = useRouter();
const authStore = useAuthStore();

const displayName = computed(() => authStore.currentUser?.username ?? '冒险者');

async function handleLogout() {
  await authStore.logout();
  await router.push({ name: 'login' });
}

onMounted(() => {
  if (authStore.isAuthenticated && !authStore.currentUser) {
    void authStore.fetchCurrentUser();
  }
});
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="232px">
      <RouterLink class="brand" to="/dashboard">
        <span class="brand-mark">LQ</span>
        <span class="brand-text">LifeQuest</span>
      </RouterLink>

      <nav class="main-nav" aria-label="主导航">
        <RouterLink
          v-for="item in navigationItems"
          :key="item.to"
          class="nav-link"
          active-class="nav-link-active"
          :to="item.to"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <p class="header-kicker">MVP Workspace</p>
          <h1>个人成长任务系统</h1>
        </div>
        <div class="header-user">
          <span v-if="authStore.isAuthenticated" class="header-username">{{ displayName }}</span>
          <button v-if="authStore.isAuthenticated" class="header-action" type="button" @click="handleLogout">
            退出
          </button>
          <RouterLink v-else class="header-action" to="/auth/login">登录</RouterLink>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
