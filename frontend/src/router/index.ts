import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/auth/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      title: '登录',
      public: true,
    },
  },
  {
    path: '/auth/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: {
      title: '注册',
      public: true,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: {
      title: '成长首页',
    },
  },
  {
    path: '/daily-log',
    name: 'daily-log',
    component: () => import('@/views/daily-log/DailyLogView.vue'),
    meta: {
      title: '每日记录',
    },
  },
  {
    path: '/settlement',
    name: 'settlement',
    component: () => import('@/views/settlement/SettlementView.vue'),
    meta: {
      title: '每日结算',
    },
  },
  {
    path: '/route',
    name: 'route',
    component: () => import('@/views/route/RouteView.vue'),
    meta: {
      title: '成长路线',
    },
  },
  {
    path: '/trends',
    name: 'trends',
    component: () => import('@/views/trends/TrendsView.vue'),
    meta: {
      title: '趋势分析',
    },
  },
  {
    path: '/weekly',
    name: 'weekly',
    component: () => import('@/views/weekly/WeeklyReportView.vue'),
    meta: {
      title: '周报',
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} - LifeQuest` : 'LifeQuest';
});

export default router;
