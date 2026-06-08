<script setup lang="ts">
const navigationItems = [
  { label: '首页', to: '/dashboard' },
  { label: '每日记录', to: '/daily-log' },
  { label: '结算', to: '/settlement' },
  { label: '路线', to: '/route' },
  { label: '趋势', to: '/trends' },
  { label: '周报', to: '/weekly' },
];
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
        <RouterLink class="header-action" to="/auth/login">登录</RouterLink>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
