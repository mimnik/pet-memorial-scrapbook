<template>
  <router-view />

  <el-dialog
    v-model="reminderDialogVisible"
    title="今日宠物提醒"
    width="680px"
    :close-on-click-modal="false"
  >
    <el-empty v-if="reminders.length === 0" description="暂无待处理提醒" />

    <div v-else class="reminder-list">
      <el-card v-for="item in reminders" :key="item.id" class="reminder-item" shadow="never">
        <div class="reminder-main">
          <div class="reminder-title-row">
            <strong>{{ item.pet?.name || '宠物' }} · {{ item.title }}</strong>
            <el-tag type="warning" effect="plain">{{ formatArchiveType(item.archiveType) }}</el-tag>
          </div>
          <p>提醒时间：{{ formatDateTime(item.reminderAt) }}</p>
          <p v-if="item.note" class="reminder-note">{{ item.note }}</p>
        </div>

        <div class="reminder-actions">
          <el-button type="success" size="small" @click="markReminderDone(item)">已完成</el-button>

          <el-dropdown @command="(hours) => onSnoozeCommand(item, Number(hours))">
            <el-button size="small">稍后</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="2">2小时后提醒</el-dropdown-item>
                <el-dropdown-item :command="24">1天后提醒</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-card>
    </div>

    <template #footer>
      <el-button @click="reminderDialogVisible = false">先关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { completeArchiveReminder, listDueArchiveReminders, snoozeArchiveReminder } from '@/api/archive'
import type { PetArchiveRecord } from '@/types/archive'

const route = useRoute()
const userStore = useUserStore()

const reminders = ref<PetArchiveRecord[]>([])
const reminderDialogVisible = ref(false)
const reminderLoading = ref(false)
const checkedInSession = ref(false)

const shouldCheckReminder = computed(() => {
  return Boolean(userStore.token) && userStore.profile?.role === 'ROLE_USER' && route.path !== '/login'
})

const archiveTypeLabelMap: Record<string, string> = {
  WEIGHT: '体重',
  VACCINE: '疫苗',
  CHECKUP: '体检',
  DEWORMING: '驱虫',
  MEDICATION: '用药',
  OTHER: '其他',
}

const formatArchiveType = (value?: string) => {
  if (!value) {
    return '其他'
  }
  return archiveTypeLabelMap[value] || value
}

const formatDateTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const removeReminder = (id: number) => {
  reminders.value = reminders.value.filter((item) => item.id !== id)
  if (reminders.value.length === 0) {
    reminderDialogVisible.value = false
  }
}

const loadDueReminders = async () => {
  if (!shouldCheckReminder.value || checkedInSession.value || reminderLoading.value) {
    return
  }

  reminderLoading.value = true
  try {
    const res = await listDueArchiveReminders()
    reminders.value = res.data
    reminderDialogVisible.value = reminders.value.length > 0
    checkedInSession.value = true
  } finally {
    reminderLoading.value = false
  }
}

const markReminderDone = async (record: PetArchiveRecord) => {
  await completeArchiveReminder(record.id)
  ElMessage.success('已标记为完成')
  removeReminder(record.id)
}

const onSnoozeCommand = async (record: PetArchiveRecord, hours: number) => {
  if (!Number.isFinite(hours) || hours <= 0) {
    return
  }

  await snoozeArchiveReminder(record.id, { delayHours: hours })
  ElMessage.success(hours === 24 ? '已设置1天后提醒' : `已设置${hours}小时后提醒`)
  removeReminder(record.id)
}

watch(
  () => `${route.path}|${userStore.token}|${userStore.profile?.role || ''}`,
  async () => {
    if (!shouldCheckReminder.value) {
      reminders.value = []
      reminderDialogVisible.value = false
      checkedInSession.value = false
      return
    }

    await loadDueReminders()
  },
  { immediate: true },
)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.reminder-list {
  display: grid;
  gap: 10px;
}

.reminder-item {
  border: 1px solid #e7edf8;
}

.reminder-main {
  display: grid;
  gap: 6px;
}

.reminder-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.reminder-main p {
  margin: 0;
  color: #5d6a82;
  font-size: 13px;
}

.reminder-note {
  white-space: pre-wrap;
}

.reminder-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .reminder-title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .reminder-actions {
    justify-content: flex-start;
  }
}
</style>
