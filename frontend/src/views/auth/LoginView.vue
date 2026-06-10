<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';

import { AppCard, AppPage } from '@/components/layout';
import { useAuthStore } from '@/stores';
import { getApiErrorMessage } from '@/utils';

interface LoginForm {
  account: string;
  password: string;
}

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();

const form = reactive<LoginForm>({
  account: '',
  password: '',
});

const rules: FormRules<LoginForm> = {
  account: [{ required: true, message: '请输入用户名、邮箱或手机号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
};

function getRedirectPath(): string {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/dashboard';
}

async function submitLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    await authStore.login({
      account: form.account.trim(),
      password: form.password,
    });
    ElMessage.success('欢迎回来，冒险者');
    await router.push(getRedirectPath());
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '登录失败，请检查账号或密码'));
  }
}
</script>

<template>
  <AppPage
    title="登录"
    eyebrow="LifeQuest Account"
    description="继续记录今天的行动，把每一次坚持转化为可见的成长。"
  >
    <div class="auth-layout">
      <AppCard title="登录冒险" subtitle="使用用户名、邮箱或手机号登录">
        <el-form
          ref="formRef"
          class="auth-form"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="submitLogin"
        >
          <el-form-item label="账号" prop="account">
            <el-input v-model.trim="form.account" autocomplete="username" placeholder="用户名 / 邮箱 / 手机号" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
              type="password"
              @keyup.enter="submitLogin"
            />
          </el-form-item>

          <el-button class="auth-submit" type="primary" native-type="submit" :loading="authStore.loading">
            登录冒险
          </el-button>
        </el-form>

        <p class="auth-switch">
          还没有角色？
          <RouterLink to="/auth/register">创建账号</RouterLink>
        </p>
      </AppCard>
    </div>
  </AppPage>
</template>

