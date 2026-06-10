<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';

import { AppCard, AppPage } from '@/components/layout';
import { useAuthStore } from '@/stores';
import { getApiErrorMessage } from '@/utils';

interface RegisterForm {
  username: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
}

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();

const form = reactive<RegisterForm>({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
});

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度应为 2-30 位', trigger: 'blur' },
  ],
  email: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value && !form.phone) {
          callback(new Error('邮箱和手机号至少填写一个'));
          return;
        }
        callback();
      },
      trigger: 'blur',
    },
    { type: 'email', message: '请输入有效邮箱', trigger: 'blur' },
  ],
  phone: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value && !form.email) {
          callback(new Error('邮箱和手机号至少填写一个'));
          return;
        }
        callback();
      },
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度应为 6-30 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'));
          return;
        }
        callback();
      },
      trigger: 'blur',
    },
  ],
};

async function submitRegister() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    await authStore.register({
      username: form.username.trim(),
      email: form.email.trim() || undefined,
      phone: form.phone.trim() || undefined,
      password: form.password,
    });
    ElMessage.success('角色创建成功');
    await router.push({ name: 'dashboard' });
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '注册失败，请稍后重试'));
  }
}
</script>

<template>
  <AppPage
    title="注册"
    eyebrow="Create Character"
    description="创建你的 LifeQuest 角色，后续将通过目标配置绑定成长路线。"
  >
    <div class="auth-layout">
      <AppCard title="创建角色" subtitle="邮箱和手机号至少填写一个">
        <el-form
          ref="formRef"
          class="auth-form"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="submitRegister"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="form.username" autocomplete="username" placeholder="例如 tiantian" />
          </el-form-item>

          <div class="auth-form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="form.email" autocomplete="email" placeholder="name@example.com" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" autocomplete="tel" placeholder="可选" />
            </el-form-item>
          </div>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              autocomplete="new-password"
              placeholder="6-30 位密码"
              show-password
              type="password"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              autocomplete="new-password"
              placeholder="再次输入密码"
              show-password
              type="password"
              @keyup.enter="submitRegister"
            />
          </el-form-item>

          <el-button class="auth-submit" type="primary" native-type="submit" :loading="authStore.loading">
            创建角色
          </el-button>
        </el-form>

        <p class="auth-switch">
          已经有账号？
          <RouterLink to="/auth/login">返回登录</RouterLink>
        </p>
      </AppCard>
    </div>
  </AppPage>
</template>
