<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">{{ isRegister ? '注册账号' : '用户登录' }}</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" maxlength="100" />
        </el-form-item>
        <el-form-item v-if="isRegister" label="邮箱">
          <el-input v-model="form.email" maxlength="120" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password maxlength="100" />
        </el-form-item>
        <el-form-item v-if="authHint">
          <el-alert :title="authHint" type="error" show-icon :closable="false" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit" style="width: 100%">
            {{ isRegister ? '注册并登录' : '登录' }}
          </el-button>
        </el-form-item>
        <el-form-item v-if="!isRegister">
          <el-button plain :loading="submitting" @click="submitGuest" style="width: 100%">
            游客登录（仅浏览宠物微社区）
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button link type="primary" @click="toggleMode">
            {{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}
          </el-button>
        </el-form-item>
        <el-form-item v-if="!isRegister">
          <el-button link type="warning" @click="openAppealDialog">账号被封禁？提交申诉</el-button>
        </el-form-item>
      </el-form>

      <el-dialog v-model="appealDialogVisible" title="账号封禁申诉" width="520px">
        <el-form :model="appealForm" label-width="92px">
          <el-form-item label="用户名" required>
            <el-input v-model="appealForm.username" maxlength="100" placeholder="请输入被冻结账号的用户名" />
          </el-form-item>
          <el-form-item label="申诉类型" required>
            <el-select v-model="appealForm.appealType" style="width: 100%">
              <el-option label="账号冻结申诉" value="ACCOUNT_FROZEN" />
              <el-option label="限制发布申诉" value="POSTING_RESTRICTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="申诉说明" required>
            <el-input
              v-model="appealForm.details"
              type="textarea"
              :rows="4"
              maxlength="2000"
              placeholder="请填写情况说明，例如误封原因、账号信息校验方式等"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="appealDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="appealSubmitting" @click="submitAppeal">提交申诉</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { createFrozenAccountAppeal } from '@/api/appeal'
import { popAuthExpiredHint } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isRegister = ref(false)
const submitting = ref(false)
const authHint = ref('')
const appealDialogVisible = ref(false)
const appealSubmitting = ref(false)
const form = ref({
  username: '',
  email: '',
  password: '',
})
const appealForm = ref({
  username: '',
  appealType: 'ACCOUNT_FROZEN',
  details: '',
})

onMounted(() => {
  const queryHint = typeof route.query.authHint === 'string' ? route.query.authHint.trim() : ''
  const expiredHint = queryHint || popAuthExpiredHint()
  if (!expiredHint) {
    return
  }

  authHint.value = expiredHint

  if (queryHint) {
    const nextQuery = { ...route.query }
    delete nextQuery.authHint
    void router.replace({ path: route.path, query: nextQuery })
  }
})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  authHint.value = ''
}

const resolveLoginTarget = (role?: string) => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  const safeRedirect = redirect.startsWith('/') ? redirect : ''

  if (role === 'ROLE_ADMIN') {
    return '/admin'
  }
  if (role === 'ROLE_GUEST') {
    if (safeRedirect.startsWith('/community')) {
      return safeRedirect
    }
    return '/community'
  }

  return safeRedirect || '/'
}

const openAppealDialog = () => {
  appealForm.value.username = form.value.username.trim()
  appealForm.value.appealType = 'ACCOUNT_FROZEN'
  appealForm.value.details = ''
  appealDialogVisible.value = true
}

const submit = async () => {
  authHint.value = ''
  if (!form.value.username.trim() || !form.value.password.trim()) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (isRegister.value && !form.value.email.trim()) {
    ElMessage.warning('请填写邮箱')
    return
  }

  submitting.value = true
  try {
    const res = isRegister.value
      ? await userStore.register({
          username: form.value.username.trim(),
          email: form.value.email.trim(),
          password: form.value.password,
        })
      : await userStore.login({
          username: form.value.username.trim(),
          password: form.value.password,
        })

    ElMessage.success(isRegister.value ? '注册成功' : '登录成功')
    const role = res.data.role || userStore.profile?.role
    await router.push(resolveLoginTarget(role))
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : ''
    if (message.includes('冻结')) {
      authHint.value = '账号已被冻结，可点击下方“账号被封禁？提交申诉”按钮提交申诉'
      return
    }
    if (message.includes('账户不存在')) {
      authHint.value = '账户不存在，请确认用户名或先注册账号'
      return
    }
    if (message.includes('密码错误')) {
      authHint.value = '密码错误，请重新输入'
      return
    }
    if (!isRegister.value) {
      authHint.value = '登录失败，请检查账号密码后重试'
    }
  } finally {
    submitting.value = false
  }
}

const submitGuest = async () => {
  authHint.value = ''
  submitting.value = true
  try {
    const res = await userStore.guestLogin()
    ElMessage.success('游客登录成功')
    await router.push(resolveLoginTarget(res.data.role))
  } catch {
    authHint.value = '游客登录失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

const submitAppeal = async () => {
  if (!appealForm.value.username.trim()) {
    ElMessage.warning('请填写用户名')
    return
  }
  if (!appealForm.value.details.trim()) {
    ElMessage.warning('请填写申诉说明')
    return
  }

  appealSubmitting.value = true
  try {
    await createFrozenAccountAppeal({
      username: appealForm.value.username.trim(),
      appealType: appealForm.value.appealType,
      details: appealForm.value.details.trim(),
    })
    ElMessage.success('申诉已提交，请等待管理员处理')
    appealDialogVisible.value = false
  } finally {
    appealSubmitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: radial-gradient(circle at 20% 0%, #ffe9d4 0%, #f4f9ff 50%, #ffffff 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 14px;
}

.title {
  margin-bottom: 14px;
  text-align: center;
}
</style>
