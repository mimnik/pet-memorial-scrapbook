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
        <el-form-item>
          <el-button link type="primary" @click="toggleMode">
            {{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const submitting = ref(false)
const authHint = ref('')
const form = ref({
  username: '',
  email: '',
  password: '',
})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  authHint.value = ''
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
    await router.push('/')
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : ''
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
