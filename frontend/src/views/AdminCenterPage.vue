<template>
  <div class="admin-page">
    <header class="admin-header">
      <div>
        <h1>管理员中心</h1>
        <p>账号管理、社区审核与系统维护统一处理面板。</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshCurrentTab">刷新当前页</el-button>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </div>
    </header>

    <el-empty v-if="!isAdmin" description="仅管理员账号可访问该页面" />

    <el-tabs v-else v-model="activeTab" class="admin-tabs">
      <el-tab-pane name="users" label="账号管理">
        <el-card shadow="hover">
          <div class="toolbar">
            <el-input v-model="userKeyword" clearable placeholder="搜索用户名/昵称/邮箱" @keyup.enter="loadUsers" />
            <el-button type="primary" :loading="usersLoading" @click="loadUsers">搜索</el-button>
          </div>

          <el-table :data="users" v-loading="usersLoading" size="small" class="table">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="displayName" label="昵称" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="200" />
            <el-table-column label="状态" min-width="170">
              <template #default="scope">
                <el-space>
                  <el-tag :type="scope.row.accountFrozen ? 'danger' : 'success'">
                    {{ scope.row.accountFrozen ? '已冻结' : '正常' }}
                  </el-tag>
                  <el-tag :type="scope.row.postingRestricted ? 'warning' : 'info'">
                    {{ scope.row.postingRestricted ? '限发中' : '可发布' }}
                  </el-tag>
                </el-space>
              </template>
            </el-table-column>
            <el-table-column label="警告" width="86">
              <template #default="scope">{{ scope.row.warningCount }}</template>
            </el-table-column>
            <el-table-column label="注册时间" width="130">
              <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="280">
              <template #default="scope">
                <el-space wrap>
                  <el-button
                    size="small"
                    type="warning"
                    plain
                    @click="toggleFreeze(scope.row)"
                  >
                    {{ scope.row.accountFrozen ? '解封' : '冻结' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="toggleRestriction(scope.row)"
                  >
                    {{ scope.row.postingRestricted ? '解除限发' : '限制发布' }}
                  </el-button>
                  <el-button size="small" type="danger" plain @click="warnUser(scope.row)">警告</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="hover" class="sub-card">
          <template #header>
            <div class="card-header">
              <span>账号异常申诉处理</span>
              <div class="toolbar-mini">
                <el-select v-model="appealStatus" style="width: 170px" @change="loadAppeals">
                  <el-option label="全部状态" value="" />
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="处理中" value="PROCESSING" />
                  <el-option label="已解决" value="RESOLVED" />
                  <el-option label="已驳回" value="REJECTED" />
                </el-select>
                <el-button @click="loadAppeals">刷新</el-button>
              </div>
            </div>
          </template>

          <el-table :data="appeals" v-loading="appealsLoading" size="small" class="table">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="username" label="申诉用户" width="120" />
            <el-table-column prop="appealType" label="类型" width="150" />
            <el-table-column prop="details" label="详情" min-width="260" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="提交时间" width="130">
              <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="处理" min-width="260">
              <template #default="scope">
                <el-space wrap>
                  <el-button size="small" @click="handleAppeal(scope.row.id, 'PROCESSING')">标记处理中</el-button>
                  <el-button size="small" type="success" @click="handleAppeal(scope.row.id, 'RESOLVED')">解决</el-button>
                  <el-button size="small" type="danger" @click="handleAppeal(scope.row.id, 'REJECTED')">驳回</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane name="moderation" label="社区审核">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>帖子审核</span>
              <div class="toolbar-mini">
                <el-input
                  v-model="postKeyword"
                  clearable
                  placeholder="搜索作者/标题/内容"
                  style="width: 240px"
                  @keyup.enter="loadPosts"
                />
                <el-button type="primary" :loading="postsLoading" @click="loadPosts">搜索</el-button>
              </div>
            </div>
          </template>

          <el-table :data="posts" v-loading="postsLoading" size="small" class="table">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="authorUsername" label="作者" width="120" />
            <el-table-column prop="petName" label="宠物" width="120" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.hiddenByAdmin ? 'warning' : 'success'">
                  {{ scope.row.hiddenByAdmin ? '已屏蔽' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="300">
              <template #default="scope">
                <el-space wrap>
                  <el-button size="small" type="warning" @click="moderatePost(scope.row.id, 'SHIELD')">屏蔽</el-button>
                  <el-button size="small" type="danger" @click="moderatePost(scope.row.id, 'DELETE')">删除</el-button>
                  <el-button size="small" plain @click="warnAuthor(scope.row.authorUsername)">警告作者</el-button>
                  <el-button size="small" plain type="primary" @click="restrictAuthor(scope.row.authorUsername)">
                    限制发布
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="hover" class="sub-card">
          <template #header>
            <div class="card-header">
              <span>评论审核</span>
              <div class="toolbar-mini">
                <el-input
                  v-model="commentKeyword"
                  clearable
                  placeholder="搜索作者/评论内容"
                  style="width: 240px"
                  @keyup.enter="loadComments"
                />
                <el-button type="primary" :loading="commentsLoading" @click="loadComments">搜索</el-button>
              </div>
            </div>
          </template>

          <el-table :data="comments" v-loading="commentsLoading" size="small" class="table">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="authorUsername" label="作者" width="120" />
            <el-table-column prop="postTitle" label="所属帖子" min-width="180" show-overflow-tooltip />
            <el-table-column prop="content" label="评论内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.hiddenByAdmin ? 'warning' : 'success'">
                  {{ scope.row.hiddenByAdmin ? '已屏蔽' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="300">
              <template #default="scope">
                <el-space wrap>
                  <el-button size="small" type="warning" @click="moderateComment(scope.row.id, 'SHIELD')">屏蔽</el-button>
                  <el-button size="small" type="danger" @click="moderateComment(scope.row.id, 'DELETE')">删除</el-button>
                  <el-button size="small" plain @click="warnAuthor(scope.row.authorUsername)">警告作者</el-button>
                  <el-button size="small" plain type="primary" @click="restrictAuthor(scope.row.authorUsername)">
                    限制发布
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="hover" class="sub-card">
          <template #header>
            <div class="card-header">
              <span>举报处理</span>
              <div class="toolbar-mini">
                <el-select v-model="reportStatus" style="width: 170px" @change="loadReports">
                  <el-option label="全部状态" value="" />
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="已通过" value="RESOLVED" />
                  <el-option label="已驳回" value="REJECTED" />
                </el-select>
                <el-button :loading="reportsLoading" @click="loadReports">刷新</el-button>
              </div>
            </div>
          </template>

          <el-table :data="reports" v-loading="reportsLoading" size="small" class="table">
            <el-table-column prop="id" label="ID" width="72" />
            <el-table-column prop="reporterUsername" label="举报人" width="120" />
            <el-table-column label="目标" width="140">
              <template #default="scope">{{ scope.row.targetType }} #{{ scope.row.targetId }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="举报原因" min-width="180" show-overflow-tooltip />
            <el-table-column prop="details" label="说明" min-width="180" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="提交时间" width="130">
              <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="360">
              <template #default="scope">
                <el-space wrap>
                  <el-button
                    size="small"
                    type="success"
                    :disabled="scope.row.status !== 'PENDING'"
                    @click="handleReportStatus(scope.row, 'RESOLVED')"
                  >
                    通过
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    :disabled="scope.row.status !== 'PENDING'"
                    @click="handleReportStatus(scope.row, 'REJECTED')"
                  >
                    驳回
                  </el-button>
                  <el-button
                    size="small"
                    type="warning"
                    plain
                    :disabled="scope.row.status !== 'PENDING'"
                    @click="moderateReportedTarget(scope.row, 'SHIELD')"
                  >
                    屏蔽目标并通过
                  </el-button>
                  <el-button
                    size="small"
                    plain
                    :disabled="scope.row.status !== 'PENDING'"
                    @click="moderateReportedTarget(scope.row, 'DELETE')"
                  >
                    删除目标并通过
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane name="dashboard" label="系统维护">
        <el-card shadow="hover" v-loading="dashboardLoading">
          <template #header>
            <div class="card-header">
              <span>系统运行总览</span>
              <el-button @click="loadDashboard">刷新</el-button>
            </div>
          </template>

          <div class="stats-grid" v-if="dashboard">
            <div class="stat-card">
              <h3>注册用户</h3>
              <p>{{ dashboard.totalUsers }}</p>
              <small>冻结 {{ dashboard.frozenUsers }} / 限发 {{ dashboard.restrictedUsers }}</small>
            </div>
            <div class="stat-card">
              <h3>社区内容</h3>
              <p>{{ dashboard.totalPosts }} 帖 / {{ dashboard.totalComments }} 评</p>
              <small>回忆 {{ dashboard.totalMemories }} 条 · 话题 {{ dashboard.totalTopics }} 个</small>
            </div>
            <div class="stat-card">
              <h3>多媒体存储</h3>
              <p>{{ dashboard.multimediaFileCount }} 文件</p>
              <small>{{ formatBytes(dashboard.multimediaStorageBytes) }}</small>
            </div>
          </div>

          <div class="health-row" v-if="dashboard">
            <el-tag :type="dashboard.authApiHealthy ? 'success' : 'danger'">
              认证接口 {{ dashboard.authApiHealthy ? '正常' : '异常' }}
            </el-tag>
            <el-tag :type="dashboard.communityApiHealthy ? 'success' : 'danger'">
              社区接口 {{ dashboard.communityApiHealthy ? '正常' : '异常' }}
            </el-tag>
            <el-tag :type="dashboard.uploadApiHealthy ? 'success' : 'danger'">
              上传接口 {{ dashboard.uploadApiHealthy ? '正常' : '异常' }}
            </el-tag>
          </div>

          <p v-if="dashboard" class="upload-path">上传目录：{{ dashboard.uploadDir }}</p>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getAdminDashboardOverview,
  handleAdminAppeal,
  listAdminAppeals,
  listAdminCommunityComments,
  listAdminCommunityPosts,
  listAdminUsers,
  moderateAdminComment,
  moderateAdminPost,
  updateAdminUserStatus,
  warnAdminUser,
} from '@/api/admin'
import { handleReport as handleContentReport, listAdminReports as listContentReports } from '@/api/report'
import type {
  AccountAppeal,
  AccountAppealHandlePayload,
  AdminCommunityComment,
  AdminCommunityPost,
  AdminDashboardOverview,
  AdminModerationPayload,
  AdminUser,
} from '@/types/admin'
import type { ReportItem, ReportStatus } from '@/types/report'

const userStore = useUserStore()
const router = useRouter()

const activeTab = ref<'users' | 'moderation' | 'dashboard'>('users')
const isAdmin = computed(() => userStore.profile?.role === 'ROLE_ADMIN')

const userKeyword = ref('')
const usersLoading = ref(false)
const users = ref<AdminUser[]>([])

const appealStatus = ref('PENDING')
const appealsLoading = ref(false)
const appeals = ref<AccountAppeal[]>([])

const postKeyword = ref('')
const postsLoading = ref(false)
const posts = ref<AdminCommunityPost[]>([])

const commentKeyword = ref('')
const commentsLoading = ref(false)
const comments = ref<AdminCommunityComment[]>([])

const reportStatus = ref<ReportStatus | ''>('PENDING')
const reportsLoading = ref(false)
const reports = ref<ReportItem[]>([])

const dashboardLoading = ref(false)
const dashboard = ref<AdminDashboardOverview | null>(null)

const logout = async () => {
  try {
    await ElMessageBox.confirm('确定退出当前管理员账号吗？', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  userStore.logout()
  ElMessage.success('已退出登录')
  await router.replace('/login')
}

const formatDate = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatBytes = (bytes: number) => {
  if (!Number.isFinite(bytes) || bytes <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let value = bytes
  let index = 0
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024
    index += 1
  }
  return `${value.toFixed(index === 0 ? 0 : 2)} ${units[index]}`
}

const loadUsers = async () => {
  usersLoading.value = true
  try {
    const res = await listAdminUsers(userKeyword.value.trim() || undefined)
    users.value = res.data
  } finally {
    usersLoading.value = false
  }
}

const loadAppeals = async () => {
  appealsLoading.value = true
  try {
    const res = await listAdminAppeals(appealStatus.value || undefined)
    appeals.value = res.data
  } finally {
    appealsLoading.value = false
  }
}

const loadPosts = async () => {
  postsLoading.value = true
  try {
    const res = await listAdminCommunityPosts(postKeyword.value.trim() || undefined)
    posts.value = res.data
  } finally {
    postsLoading.value = false
  }
}

const loadComments = async () => {
  commentsLoading.value = true
  try {
    const res = await listAdminCommunityComments({ keyword: commentKeyword.value.trim() || undefined })
    comments.value = res.data
  } finally {
    commentsLoading.value = false
  }
}

const loadReports = async () => {
  reportsLoading.value = true
  try {
    const res = await listContentReports((reportStatus.value || undefined) as ReportStatus | undefined)
    reports.value = res.data
  } finally {
    reportsLoading.value = false
  }
}

const loadDashboard = async () => {
  dashboardLoading.value = true
  try {
    const res = await getAdminDashboardOverview()
    dashboard.value = res.data
  } finally {
    dashboardLoading.value = false
  }
}

const findUserByUsername = (username: string) => users.value.find((item) => item.username === username)

const ensureUserLoaded = async (username: string) => {
  if (findUserByUsername(username)) {
    return
  }
  const res = await listAdminUsers(username)
  for (const user of res.data) {
    if (!users.value.some((item) => item.id === user.id)) {
      users.value.push(user)
    }
  }
}

const toggleFreeze = async (user: AdminUser) => {
  const note = await ElMessageBox.prompt('可选：填写处理说明', user.accountFrozen ? '解封账号' : '冻结账号', {
    inputPlaceholder: '例如：违规发布广告',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '说明不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await updateAdminUserStatus(user.id, {
    accountFrozen: !user.accountFrozen,
    adminNote: note.value?.trim() || undefined,
  })
  ElMessage.success(user.accountFrozen ? '已解封' : '已冻结')
  await loadUsers()
}

const toggleRestriction = async (user: AdminUser) => {
  const note = await ElMessageBox.prompt('可选：填写处理说明', user.postingRestricted ? '解除限制发布' : '限制发布', {
    inputPlaceholder: '例如：多次发布无关内容',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '说明不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await updateAdminUserStatus(user.id, {
    postingRestricted: !user.postingRestricted,
    adminNote: note.value?.trim() || undefined,
  })
  ElMessage.success(user.postingRestricted ? '已解除限制发布' : '已限制发布')
  await loadUsers()
}

const warnUser = async (user: AdminUser) => {
  const note = await ElMessageBox.prompt('填写警告说明（可选）', '警告用户', {
    inputPlaceholder: '例如：请勿重复发布广告信息',
    confirmButtonText: '确认警告',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '说明不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await warnAdminUser(user.id, { note: note.value?.trim() || undefined })
  ElMessage.success('警告已记录')
  await loadUsers()
}

const handleAppeal = async (id: number, status: AccountAppealHandlePayload['status']) => {
  const note = await ElMessageBox.prompt('填写处理说明（可选）', `申诉处理：${status}`, {
    inputPlaceholder: '例如：已核验身份，完成重置',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '说明不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await handleAdminAppeal(id, {
    status,
    handleNote: note.value?.trim() || undefined,
  })
  ElMessage.success('申诉状态已更新')
  await loadAppeals()
}

const moderatePost = async (id: number, action: AdminModerationPayload['action']) => {
  const note = await ElMessageBox.prompt('填写审核原因（可选）', action === 'DELETE' ? '删除帖子' : '屏蔽帖子', {
    inputPlaceholder: '例如：含不良信息',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '原因不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await moderateAdminPost(id, {
    action,
    reason: note.value?.trim() || undefined,
  })
  ElMessage.success(action === 'DELETE' ? '帖子已删除' : '帖子已屏蔽')
  await loadPosts()
}

const moderateComment = async (id: number, action: AdminModerationPayload['action']) => {
  const note = await ElMessageBox.prompt('填写审核原因（可选）', action === 'DELETE' ? '删除评论' : '屏蔽评论', {
    inputPlaceholder: '例如：人身攻击',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '原因不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await moderateAdminComment(id, {
    action,
    reason: note.value?.trim() || undefined,
  })
  ElMessage.success(action === 'DELETE' ? '评论已删除' : '评论已屏蔽')
  await loadComments()
}

const handleReportStatus = async (report: ReportItem, status: Exclude<ReportStatus, 'PENDING'>) => {
  if (report.status !== 'PENDING') {
    ElMessage.info('该举报已处理')
    return
  }

  const note = await ElMessageBox.prompt('填写处理说明（可选）', status === 'RESOLVED' ? '通过举报' : '驳回举报', {
    inputPlaceholder: '例如：已核验并处理',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '说明不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await handleContentReport(report.id, {
    status,
    handleNote: note.value?.trim() || undefined,
  })
  ElMessage.success(status === 'RESOLVED' ? '举报已通过' : '举报已驳回')
  await loadReports()
}

const moderateReportedTarget = async (report: ReportItem, action: AdminModerationPayload['action']) => {
  if (report.status !== 'PENDING') {
    ElMessage.info('仅待处理举报可执行内容处置')
    return
  }

  const isPost = report.targetType === 'POST'
  const isComment = report.targetType === 'COMMENT'
  if (!isPost && !isComment) {
    ElMessage.warning('暂不支持该举报目标类型')
    return
  }

  const note = await ElMessageBox.prompt(
    '填写处置原因（可选）',
    action === 'DELETE' ? '删除目标内容并通过举报' : '屏蔽目标内容并通过举报',
    {
      inputPlaceholder: '例如：核验违规属实',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputValidator: (value) => (value.length <= 1000 ? true : '原因不能超过1000字'),
    },
  ).catch(() => null)

  if (note === null) {
    return
  }

  const reason = note.value?.trim() || undefined
  if (isPost) {
    await moderateAdminPost(report.targetId, { action, reason })
  } else {
    await moderateAdminComment(report.targetId, { action, reason })
  }

  await handleContentReport(report.id, {
    status: 'RESOLVED',
    handleNote: reason || (action === 'DELETE' ? '已删除被举报内容' : '已屏蔽被举报内容'),
  })

  ElMessage.success(action === 'DELETE' ? '已删除目标并通过举报' : '已屏蔽目标并通过举报')
  await Promise.all([loadReports(), loadPosts(), loadComments()])
}

const warnAuthor = async (username: string) => {
  await ensureUserLoaded(username)
  const target = findUserByUsername(username)
  if (!target) {
    ElMessage.warning('未找到该作者对应账号')
    return
  }
  await warnUser(target)
}

const restrictAuthor = async (username: string) => {
  await ensureUserLoaded(username)
  const target = findUserByUsername(username)
  if (!target) {
    ElMessage.warning('未找到该作者对应账号')
    return
  }
  if (target.postingRestricted) {
    ElMessage.info('该用户已处于限制发布状态')
    return
  }

  const note = await ElMessageBox.prompt('填写限制发布原因（可选）', '限制发布', {
    inputPlaceholder: '例如：连续发布违规内容',
    confirmButtonText: '确认限制',
    cancelButtonText: '取消',
    inputValidator: (value) => (value.length <= 1000 ? true : '原因不能超过1000字'),
  }).catch(() => null)

  if (note === null) {
    return
  }

  await updateAdminUserStatus(target.id, {
    postingRestricted: true,
    adminNote: note.value?.trim() || undefined,
  })
  ElMessage.success('已限制该用户发布权限')
  await loadUsers()
}

const refreshCurrentTab = async () => {
  if (activeTab.value === 'users') {
    await Promise.all([loadUsers(), loadAppeals()])
    return
  }
  if (activeTab.value === 'moderation') {
    await Promise.all([loadPosts(), loadComments(), loadReports()])
    return
  }
  await loadDashboard()
}

onMounted(async () => {
  await userStore.refreshProfile()
  if (!isAdmin.value) {
    return
  }
  await Promise.all([loadUsers(), loadAppeals(), loadPosts(), loadComments(), loadReports(), loadDashboard()])
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 20px;
  background:
    radial-gradient(circle at 0% 0%, rgba(255, 232, 214, 0.46) 0%, rgba(255, 232, 214, 0) 33%),
    radial-gradient(circle at 100% 0%, rgba(198, 227, 255, 0.4) 0%, rgba(198, 227, 255, 0) 34%),
    linear-gradient(180deg, #fff9f3 0%, #f4f9ff 48%, #ffffff 100%);
}

.admin-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  align-items: flex-start;
}

.admin-header h1 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #243049;
}

.admin-header p {
  margin: 0;
  color: #657389;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-tabs :deep(.el-tabs__content) {
  margin-top: 8px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.toolbar-mini {
  display: flex;
  gap: 8px;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.table {
  width: 100%;
}

.sub-card {
  margin-top: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.stat-card {
  border: 1px solid #e4ebf8;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}

.stat-card h3 {
  margin: 0 0 8px;
  color: #334258;
}

.stat-card p {
  font-size: 24px;
  margin-bottom: 6px;
  color: #1f2a40;
}

.stat-card small {
  color: #6a778f;
}

.health-row {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.upload-path {
  margin-top: 12px;
  color: #64728a;
}

@media (max-width: 900px) {
  .admin-header {
    flex-direction: column;
  }

  .toolbar,
  .card-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
