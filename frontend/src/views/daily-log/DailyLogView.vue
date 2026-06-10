<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';

import { dailyLogApi } from '@/api';
import { AppCard, AppPage } from '@/components/layout';
import type { DailyLogSourceType, SubmitDailyLogRequest } from '@/types';
import { getApiErrorMessage } from '@/utils';

interface TaskItem {
  id: number;
  title: string;
  completed: boolean;
}

interface DailyLogForm {
  logDate: string;
  studyHours: number;
  workHours: number;
  sleepHours: number;
  exerciseMinutes: number;
  entertainmentMinutes: number;
  moodTag: string;
  taskCompletionRate: number;
  completedContent: string;
  problemText: string;
  reflectionText: string;
  rawText: string;
}

const router = useRouter();
const formRef = ref<FormInstance>();
const submitting = ref(false);
const nextTaskId = ref(4);
const newTaskTitle = ref('');

const form = reactive<DailyLogForm>({
  logDate: new Date().toLocaleDateString('en-CA'),
  studyHours: 0,
  workHours: 0,
  sleepHours: 7,
  exerciseMinutes: 0,
  entertainmentMinutes: 0,
  moodTag: '还不错',
  taskCompletionRate: 0,
  completedContent: '',
  problemText: '',
  reflectionText: '',
  rawText: '',
});

const tasks = ref<TaskItem[]>([
  { id: 1, title: '完成今日核心学习任务', completed: false },
  { id: 2, title: '推进项目或工作事项', completed: false },
  { id: 3, title: '完成一次复盘记录', completed: false },
]);

const moodOptions = ['非常好', '还不错', '一般', '有点差', '很糟糕'];

const rules: FormRules<DailyLogForm> = {
  logDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
  studyHours: [
    { required: true, message: '请输入学习时长', trigger: 'blur' },
    { type: 'number', min: 0, max: 24, message: '学习时长需在 0-24 小时之间', trigger: 'blur' },
  ],
  workHours: [{ type: 'number', min: 0, max: 24, message: '工作/项目时长需在 0-24 小时之间', trigger: 'blur' }],
  sleepHours: [
    { required: true, message: '请输入睡眠时长', trigger: 'blur' },
    { type: 'number', min: 0, max: 24, message: '睡眠时长需在 0-24 小时之间', trigger: 'blur' },
  ],
  exerciseMinutes: [{ type: 'number', min: 0, max: 1440, message: '运动时长需在 0-1440 分钟之间', trigger: 'blur' }],
  entertainmentMinutes: [
    { type: 'number', min: 0, max: 1440, message: '娱乐时长需在 0-1440 分钟之间', trigger: 'blur' },
  ],
  taskCompletionRate: [
    { required: true, message: '请输入任务完成率', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '任务完成率需在 0-100 之间', trigger: 'blur' },
  ],
  reflectionText: [{ max: 1000, message: '今日复盘不超过 1000 字', trigger: 'blur' }],
  rawText: [{ max: 2000, message: '自然语言日志不超过 2000 字', trigger: 'blur' }],
};

const completedTaskCount = computed(() => tasks.value.filter((task) => task.completed).length);

function syncCompletionRate() {
  if (!tasks.value.length) {
    return;
  }

  form.taskCompletionRate = Math.round((completedTaskCount.value / tasks.value.length) * 100);
}

function addTask() {
  const title = newTaskTitle.value.trim();
  if (!title) {
    return;
  }

  tasks.value.push({
    id: nextTaskId.value,
    title,
    completed: false,
  });
  nextTaskId.value += 1;
  newTaskTitle.value = '';
  syncCompletionRate();
}

function removeTask(taskId: number) {
  tasks.value = tasks.value.filter((task) => task.id !== taskId);
  syncCompletionRate();
}

function inferSourceType(): DailyLogSourceType {
  const hasRawText = Boolean(form.rawText.trim());
  const hasFormContent = Boolean(
    form.completedContent.trim() ||
      form.problemText.trim() ||
      form.reflectionText.trim() ||
      form.studyHours ||
      form.workHours ||
      form.exerciseMinutes ||
      form.entertainmentMinutes,
  );

  if (hasRawText && hasFormContent) {
    return 'MIXED';
  }

  return hasRawText ? 'NATURAL_LANGUAGE' : 'FORM';
}

function buildCompletedContent(): string | undefined {
  const taskLines = tasks.value
    .filter((task) => task.title.trim())
    .map((task) => `${task.completed ? '[已完成]' : '[未完成]'} ${task.title.trim()}`);
  const content = [form.completedContent.trim(), ...taskLines].filter(Boolean).join('\n');
  return content || undefined;
}

function toPayload(): SubmitDailyLogRequest {
  return {
    logDate: form.logDate,
    rawText: form.rawText.trim() || undefined,
    studyHours: form.studyHours,
    workHours: form.workHours,
    sleepHours: form.sleepHours,
    exerciseMinutes: form.exerciseMinutes,
    entertainmentMinutes: form.entertainmentMinutes,
    moodTag: form.moodTag,
    taskCompletionRate: form.taskCompletionRate,
    completedContent: buildCompletedContent(),
    problemText: form.problemText.trim() || undefined,
    reflectionText: form.reflectionText.trim() || undefined,
    sourceType: inferSourceType(),
  };
}

async function submitDailyLog() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  try {
    await dailyLogApi.submitSettlement(toPayload());
    ElMessage.success('今日记录已提交，正在进入结算');
    await router.push({
      name: 'settlement',
      query: { date: form.logDate },
    });
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '提交失败，请检查记录内容后重试'));
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <AppPage
    title="每日记录"
    eyebrow="Daily Log"
    description="记录今天的时间分配、任务完成、情绪和复盘，提交后会生成今日结算。"
  >
    <el-form
      ref="formRef"
      class="daily-log-form"
      :model="form"
      :rules="rules"
      label-position="top"
      @submit.prevent="submitDailyLog"
    >
      <div class="daily-log-grid">
        <AppCard title="结构化记录" subtitle="这些字段会直接参与规则评分">
          <div class="daily-log-card-grid">
            <el-form-item label="记录日期" prop="logDate">
              <el-date-picker v-model="form.logDate" type="date" value-format="YYYY-MM-DD" class="full-field" />
            </el-form-item>

            <el-form-item label="今日心情" prop="moodTag">
              <el-select v-model="form.moodTag" class="full-field">
                <el-option v-for="mood in moodOptions" :key="mood" :label="mood" :value="mood" />
              </el-select>
            </el-form-item>

            <el-form-item label="学习时长（小时）" prop="studyHours">
              <el-input-number v-model="form.studyHours" :min="0" :max="24" :step="0.5" class="full-field" />
            </el-form-item>

            <el-form-item label="工作/项目时长（小时）" prop="workHours">
              <el-input-number v-model="form.workHours" :min="0" :max="24" :step="0.5" class="full-field" />
            </el-form-item>

            <el-form-item label="睡眠时长（小时）" prop="sleepHours">
              <el-input-number v-model="form.sleepHours" :min="0" :max="24" :step="0.5" class="full-field" />
            </el-form-item>

            <el-form-item label="运动时长（分钟）" prop="exerciseMinutes">
              <el-input-number v-model="form.exerciseMinutes" :min="0" :max="1440" :step="5" class="full-field" />
            </el-form-item>

            <el-form-item label="娱乐时长（分钟）" prop="entertainmentMinutes">
              <el-input-number v-model="form.entertainmentMinutes" :min="0" :max="1440" :step="5" class="full-field" />
            </el-form-item>

            <el-form-item label="任务完成率（%）" prop="taskCompletionRate">
              <el-input-number v-model="form.taskCompletionRate" :min="0" :max="100" :step="5" class="full-field" />
            </el-form-item>
          </div>
        </AppCard>

        <AppCard title="自然语言日志" subtitle="LLM 可用时会尝试解析，失败也不会阻断结算">
          <el-form-item label="原始日志" prop="rawText">
            <el-input
              v-model="form.rawText"
              type="textarea"
              :rows="8"
              maxlength="2000"
              show-word-limit
              placeholder="例如：今天上午复习 Redis，下午写了接口，晚上有点分心，运动了 20 分钟。"
            />
          </el-form-item>
        </AppCard>
      </div>

      <AppCard title="今日任务清单" subtitle="用于辅助填写完成内容和任务完成率">
        <div class="task-list">
          <label v-for="task in tasks" :key="task.id" class="task-item">
            <el-checkbox v-model="task.completed" @change="syncCompletionRate" />
            <span>{{ task.title }}</span>
            <el-button text type="danger" @click="removeTask(task.id)">删除</el-button>
          </label>
        </div>

        <div class="task-add-row">
          <el-input v-model.trim="newTaskTitle" placeholder="添加一个今日任务" @keyup.enter="addTask" />
          <el-button type="primary" plain @click="addTask">添加任务</el-button>
        </div>
      </AppCard>

      <AppCard title="今日复盘" subtitle="把完成内容、问题和下一次改进点写清楚">
        <el-form-item label="今日完成内容" prop="completedContent">
          <el-input
            v-model="form.completedContent"
            type="textarea"
            :rows="3"
            placeholder="例如：复习 Redis 持久化，完成每日结算接口联调。"
          />
        </el-form-item>

        <el-form-item label="今日遇到的问题" prop="problemText">
          <el-input
            v-model="form.problemText"
            type="textarea"
            :rows="3"
            placeholder="例如：晚间注意力下降，任务切换太频繁。"
          />
        </el-form-item>

        <el-form-item label="今日复盘和改进计划" prop="reflectionText">
          <el-input
            v-model="form.reflectionText"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="写下今天最值得保留的做法，以及明天需要防御的问题。"
          />
        </el-form-item>
      </AppCard>

      <div class="daily-log-actions">
        <el-button type="primary" size="large" native-type="submit" :loading="submitting">
          提交今日记录
        </el-button>
      </div>
    </el-form>
  </AppPage>
</template>

