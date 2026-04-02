<template>
  <div class="home-page">
    <header class="hero">
      <div class="hero-title-row">
        <div class="hero-copy">
          <p class="hero-eyebrow">PET MEMORIAL SCRAPBOOK</p>
          <h1>宠物数字纪念册</h1>
          <p>记录每一段与宠物同行的回忆，沉淀成为可长期保存的数字纪念页。</p>
          <p class="login-user">
            当前登录：<strong>@{{ userStore.profile?.username || '未登录' }}</strong>
            <span v-if="userDisplayName"> · {{ userDisplayName }}</span>
          </p>
        </div>
        <div class="hero-actions">
          <el-button v-if="!isAdmin" type="primary" plain @click="goCommunity">进入宠物社区</el-button>
          <el-button v-if="isAdmin" type="warning" plain @click="goAdminCenter">管理员中心</el-button>
          <el-button v-if="!isAdmin" @click="openFollowingDialog">关注动态</el-button>
          <el-button v-if="!isAdmin" @click="copyShareLink">分享主页</el-button>
          <el-button type="danger" plain @click="logout">退出登录</el-button>

          <el-popover v-if="!isAdmin" placement="bottom-end" trigger="hover" :width="320">
            <template #reference>
              <button class="avatar-trigger" type="button">
                <img :src="currentAvatarUrl" alt="user-avatar" class="user-avatar" />
                <span>{{ userDisplayName || `@${userStore.profile?.username || ''}` }}</span>
              </button>
            </template>

            <div class="user-center-panel">
              <div class="user-center-head">
                <img :src="currentAvatarUrl" alt="user-avatar" class="user-center-avatar" />
                <div>
                  <strong>{{ userDisplayName || userStore.profile?.username }}</strong>
                  <p>@{{ userStore.profile?.username }}</p>
                </div>
              </div>

              <p v-if="profile?.bio" class="user-center-bio">{{ profile?.bio }}</p>
              <div class="user-center-stats">
                <span>关注 {{ profile?.followingCount || 0 }}</span>
                <span>粉丝 {{ profile?.followerCount || 0 }}</span>
              </div>

              <div class="user-center-actions">
                <el-button type="primary" size="small" @click="openProfileDialog">用户中心</el-button>
                <el-button size="small" @click="openFollowingDialog">关注动态</el-button>
              </div>
            </div>
          </el-popover>
        </div>
      </div>
    </header>

    <el-card v-if="isAdmin" class="panel" shadow="hover">
      <template #header>
        <div class="panel-header">
          <span>管理员账号</span>
          <el-button type="warning" @click="goAdminCenter">进入管理中心</el-button>
        </div>
      </template>
      <p>管理员账号仅保留管理中心功能，普通宠物记录与分享功能已隐藏。</p>
    </el-card>

    <el-row v-if="!isAdmin" :gutter="20">
      <el-col :xs="24" :lg="10">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span>我的宠物</span>
              <el-button type="primary" @click="openCreatePetDialog">新增宠物</el-button>
            </div>
          </template>

          <el-empty v-if="pets.length === 0" description="还没有宠物档案，先创建一个吧" />

          <div v-else class="pet-list">
            <div
              v-for="pet in pets"
              :key="pet.id"
              class="pet-item"
              :class="{ active: selectedPetId === pet.id }"
              @click="selectPet(pet.id)"
            >
              <div class="pet-item-main">
                <img v-if="pet.avatarUrl" :src="pet.avatarUrl" alt="pet-avatar" class="pet-item-avatar" />
                <div>
                  <h3>{{ pet.name }}</h3>
                  <p>{{ [pet.species, pet.breed].filter(Boolean).join(' / ') || '未填写物种信息' }}</p>
                </div>
              </div>
              <div class="pet-actions">
                <el-button link @click.stop="goPetDetail(pet.id)">详情</el-button>
                <el-button link type="primary" @click.stop="openEditPetDialog(pet)">编辑</el-button>
                <el-button link type="danger" @click.stop="onDeletePet(pet.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span>回忆时间线</span>
              <el-button type="success" :disabled="!selectedPetId" @click="openCreateMemoryDialog">
                新增回忆
              </el-button>
            </div>
          </template>

          <div v-if="selectedPet" class="selected-pet">
            当前宠物：<strong>{{ selectedPet.name }}</strong>
            <el-tag size="small" :type="selectedPet.isPublic ? 'success' : 'info'" style="margin-left: 8px">
              {{ selectedPet.isPublic ? '公开中' : '私密' }}
            </el-tag>
          </div>

          <el-empty v-if="!selectedPetId" description="请先在左侧选择一个宠物" />
          <el-empty v-else-if="memories.length === 0" description="还没有回忆条目，开始记录第一条吧" />

          <el-timeline v-else>
            <el-timeline-item
              v-for="memory in memories"
              :key="memory.id"
              :timestamp="memory.eventDate || '未填写日期'"
              placement="top"
            >
              <el-card class="memory-card">
                <div class="memory-title-row">
                  <h3>{{ memory.title }}</h3>
                  <div>
                    <el-button link type="primary" @click="openEditMemoryDialog(memory)">编辑</el-button>
                    <el-button link type="danger" @click="onDeleteMemory(memory.id)">删除</el-button>
                  </div>
                </div>
                <p>{{ memory.content }}</p>
                <p v-if="memory.location" class="memory-meta">地点：{{ memory.location }}</p>
                <img v-if="memory.imageUrl" :src="memory.imageUrl" alt="memory-image" class="memory-image" />
                <div v-if="memory.videoUrl" class="memory-video-wrapper">
                  <video
                    class="memory-video"
                    controls
                    preload="metadata"
                    :poster="memory.videoCoverUrl || undefined"
                    :src="memory.videoUrl"
                  />
                  <span v-if="memory.videoDurationSeconds" class="video-duration-chip">
                    {{ formatDuration(memory.videoDurationSeconds) }}
                  </span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="followingDialogVisible" title="关注动态" width="760px">
      <div class="following-dialog-header">
        <span>你关注的用户最近发布内容</span>
        <el-button type="primary" link @click="loadFollowingFeed">刷新</el-button>
      </div>

      <el-empty v-if="followingPosts.length === 0" description="还没有关注动态，去社区关注一些用户吧" />

      <div v-else class="following-feed-list">
        <div v-for="post in followingPosts" :key="post.id" class="following-feed-item">
          <div>
            <strong>{{ post.title }}</strong>
            <p>
              @{{ post.authorUsername }}
              <span v-if="post.topicName"> · #{{ post.topicName }}</span>
            </p>
          </div>
          <div class="following-feed-actions">
            <span>{{ formatPostTime(post.createdAt) }}</span>
            <el-button link type="primary" @click="goCommunity">去查看</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="petDialogVisible" :title="petDialogTitle" width="520px">
      <el-form :model="petForm" label-width="88px">
        <el-form-item label="宠物昵称" required>
          <el-input v-model="petForm.name" maxlength="100" placeholder="例如：可乐" />
        </el-form-item>
        <el-form-item label="物种">
          <el-input v-model="petForm.species" placeholder="例如：猫" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="petForm.breed" placeholder="例如：英短" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="petForm.gender" placeholder="请选择性别" clearable>
            <el-option
              v-for="option in petGenderOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="petForm.birthDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="纪念日">
          <el-date-picker v-model="petForm.memorialDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="头像链接">
          <div class="upload-row">
            <el-input v-model="petForm.avatarUrl" placeholder="上传后自动填写或手动输入 https://..." />
            <el-upload :show-file-list="false" :http-request="uploadPetAvatar">
              <el-button>上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="公开分享">
          <el-switch v-model="petForm.isPublic" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="petForm.description" type="textarea" :rows="3" maxlength="2000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPet">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profileDialogVisible" title="个人资料" width="520px">
      <el-form :model="profileForm" label-width="96px" v-loading="profileLoading">
        <el-form-item label="用户名">
          <el-input :model-value="profile?.username || ''" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :model-value="profile?.email || ''" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.displayName" maxlength="100" placeholder="可用于首页展示" />
        </el-form-item>
        <el-form-item label="头像链接">
          <div class="upload-row">
            <el-input v-model="profileForm.avatarUrl" placeholder="上传后自动填写或手动输入 https://..." />
            <el-upload :show-file-list="false" :http-request="uploadProfileAvatar" accept="image/*">
              <el-button>上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="profileForm.bio" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>
        <el-form-item label="关注数据">
          <el-space>
            <el-tag type="info">关注 {{ profile?.followingCount || 0 }}</el-tag>
            <el-tag type="success">粉丝 {{ profile?.followerCount || 0 }}</el-tag>
          </el-space>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSubmitting" @click="submitProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="memoryDialogVisible" :title="memoryDialogTitle" width="520px">
      <el-form :model="memoryForm" label-width="88px">
        <el-form-item label="标题" required>
          <el-input v-model="memoryForm.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="memoryForm.content" type="textarea" :rows="4" maxlength="5000" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="memoryForm.eventDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="memoryForm.location" maxlength="200" />
        </el-form-item>
        <el-form-item label="图片链接">
          <div class="upload-row">
            <el-input v-model="memoryForm.imageUrl" placeholder="上传后自动填写或手动输入 https://..." />
            <el-upload :show-file-list="false" :http-request="uploadMemoryImage">
              <el-button>上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="视频链接">
          <div class="upload-row">
            <el-input v-model="memoryForm.videoUrl" placeholder="上传后自动填写或手动输入 https://..." />
            <el-upload :show-file-list="false" :http-request="uploadMemoryVideo" accept="video/*">
              <el-button>上传视频</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="视频时长">
          <el-input :model-value="memoryForm.videoDurationSeconds ? formatDuration(memoryForm.videoDurationSeconds) : ''" disabled placeholder="上传视频后自动计算" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitMemory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { createPet, deletePet, listPets, updatePet } from '@/api/pet'
import { createMemory, deleteMemory, listMemoriesByPet, updateMemory } from '@/api/memory'
import { listFollowingCommunityFeed } from '@/api/community'
import { getMyProfile, updateMyProfile } from '@/api/user'
import { uploadImage, uploadMedia } from '@/api/file'
import type { CommunityPost } from '@/types/community'
import type { Pet, PetPayload } from '@/types/pet'
import type { MemoryEntry, MemoryEntryPayload } from '@/types/memory'
import type { UserProfile } from '@/types/user'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const pets = ref<Pet[]>([])
const memories = ref<MemoryEntry[]>([])
const followingPosts = ref<CommunityPost[]>([])
const selectedPetId = ref<number | null>(null)
const submitting = ref(false)
const defaultAvatarUrl = '/default-avatar.jpg'

const petDialogVisible = ref(false)
const memoryDialogVisible = ref(false)
const profileDialogVisible = ref(false)
const followingDialogVisible = ref(false)
const editingPetId = ref<number | null>(null)
const editingMemoryId = ref<number | null>(null)
const profileLoading = ref(false)
const profileSubmitting = ref(false)
const profile = ref<UserProfile | null>(null)

const petForm = ref<PetPayload>({
  name: '',
  species: '',
  breed: '',
  gender: '',
  birthDate: '',
  memorialDate: '',
  avatarUrl: '',
  description: '',
  isPublic: false,
})

const memoryForm = ref<MemoryEntryPayload>({
  title: '',
  content: '',
  eventDate: new Date().toISOString().slice(0, 10),
  location: '',
  imageUrl: '',
  videoUrl: '',
  videoCoverUrl: '',
  videoDurationSeconds: undefined,
})

const profileForm = ref({
  displayName: '',
  avatarUrl: '',
  bio: '',
})

const selectedPet = computed(() => pets.value.find((pet) => pet.id === selectedPetId.value))
const petDialogTitle = computed(() => (editingPetId.value ? '编辑宠物' : '新增宠物'))
const memoryDialogTitle = computed(() => (editingMemoryId.value ? '编辑回忆' : '新增回忆'))
const isAdmin = computed(() => userStore.profile?.role === 'ROLE_ADMIN')
const userDisplayName = computed(() => profile.value?.displayName || userStore.profile?.displayName || '')
const currentAvatarUrl = computed(() => profile.value?.avatarUrl || userStore.profile?.avatarUrl || defaultAvatarUrl)
const petGenderOptions = [
  { label: '公', value: '公' },
  { label: '母', value: '母' },
  { label: '未知', value: '未知' },
]

const sanitizePayload = <T extends Record<string, unknown>>(payload: T) => {
  return Object.fromEntries(
    Object.entries(payload).map(([k, v]) => [k, typeof v === 'string' ? v.trim() : v]),
  ) as T
}

const formatDuration = (seconds: number) => {
  const safe = Math.max(0, Math.floor(seconds))
  const hour = Math.floor(safe / 3600)
  const minute = Math.floor((safe % 3600) / 60)
  const second = safe % 60

  if (hour > 0) {
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return `${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
}

const formatPostTime = (value: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const readVideoDuration = (file: File): Promise<number> =>
  new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.src = url

    video.onloadedmetadata = () => {
      URL.revokeObjectURL(url)
      resolve(Math.max(1, Math.round(video.duration || 0)))
    }
    video.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('视频元数据读取失败'))
    }
  })

const captureVideoCover = (file: File): Promise<Blob | null> =>
  new Promise((resolve) => {
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.muted = true
    video.playsInline = true
    video.src = url

    const finish = (blob: Blob | null) => {
      URL.revokeObjectURL(url)
      resolve(blob)
    }

    video.onloadeddata = () => {
      const width = video.videoWidth || 640
      const height = video.videoHeight || 360
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const context = canvas.getContext('2d')
      if (!context) {
        finish(null)
        return
      }
      context.drawImage(video, 0, 0, width, height)
      canvas.toBlob(
        (blob) => finish(blob),
        'image/jpeg',
        0.82,
      )
    }

    video.onerror = () => finish(null)
  })

const loadPets = async () => {
  const res = await listPets()
  pets.value = res.data
  if (!selectedPetId.value && pets.value.length > 0) {
    const firstPet = pets.value[0]
    if (firstPet) {
      selectedPetId.value = firstPet.id
      await loadMemories()
    }
  }
  if (selectedPetId.value && pets.value.every((pet) => pet.id !== selectedPetId.value)) {
    selectedPetId.value = pets.value[0]?.id ?? null
    await loadMemories()
  }
}

const loadMemories = async () => {
  if (!selectedPetId.value) {
    memories.value = []
    return
  }
  const res = await listMemoriesByPet(selectedPetId.value)
  memories.value = res.data
}

const loadFollowingFeed = async () => {
  const res = await listFollowingCommunityFeed()
  followingPosts.value = res.data
}

const openFollowingDialog = async () => {
  followingDialogVisible.value = true
  await loadFollowingFeed()
}

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const res = await getMyProfile()
    profile.value = res.data
    profileForm.value = {
      displayName: res.data.displayName || '',
      avatarUrl: res.data.avatarUrl || '',
      bio: res.data.bio || '',
    }
    if (userStore.profile) {
      userStore.profile.displayName = res.data.displayName
      userStore.profile.avatarUrl = res.data.avatarUrl
    }
  } finally {
    profileLoading.value = false
  }
}

const resetPetForm = () => {
  petForm.value = {
    name: '',
    species: '',
    breed: '',
    gender: '',
    birthDate: '',
    memorialDate: '',
    avatarUrl: '',
    description: '',
    isPublic: false,
  }
}

const resetMemoryForm = () => {
  memoryForm.value = {
    title: '',
    content: '',
    eventDate: new Date().toISOString().slice(0, 10),
    location: '',
    imageUrl: '',
    videoUrl: '',
    videoCoverUrl: '',
    videoDurationSeconds: undefined,
  }
}

const openProfileDialog = async () => {
  if (!profile.value) {
    await loadProfile()
  }
  profileDialogVisible.value = true
}

const openCreatePetDialog = () => {
  editingPetId.value = null
  resetPetForm()
  petDialogVisible.value = true
}

const openEditPetDialog = (pet: Pet) => {
  editingPetId.value = pet.id
  petForm.value = {
    name: pet.name,
    species: pet.species || '',
    breed: pet.breed || '',
    gender: pet.gender || '',
    birthDate: pet.birthDate || '',
    memorialDate: pet.memorialDate || '',
    avatarUrl: pet.avatarUrl || '',
    description: pet.description || '',
    isPublic: pet.isPublic,
  }
  petDialogVisible.value = true
}

const uploadPetAvatar = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const res = await uploadImage(file)
  petForm.value.avatarUrl = res.data.url
  ElMessage.success('头像上传成功')
}

const uploadMemoryImage = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const res = await uploadImage(file)
  memoryForm.value.imageUrl = res.data.url
  ElMessage.success('图片上传成功')
}

const uploadMemoryVideo = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const [uploadRes, duration, coverBlob] = await Promise.all([
    uploadMedia(file),
    readVideoDuration(file).catch(() => undefined),
    captureVideoCover(file),
  ])

  if (uploadRes.data.mediaType !== 'video') {
    ElMessage.warning('请选择视频文件上传')
    return
  }

  memoryForm.value.videoUrl = uploadRes.data.url
  memoryForm.value.videoDurationSeconds = duration

  if (coverBlob) {
    const coverRes = await uploadImage(coverBlob, `${Date.now()}-memory-cover.jpg`)
    memoryForm.value.videoCoverUrl = coverRes.data.url
  }

  ElMessage.success('视频上传成功')
}

const uploadProfileAvatar = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const res = await uploadImage(file)
  profileForm.value.avatarUrl = res.data.url
  ElMessage.success('头像上传成功')
}

const submitProfile = async () => {
  profileSubmitting.value = true
  try {
    const res = await updateMyProfile({
      displayName: profileForm.value.displayName.trim() || undefined,
      avatarUrl: profileForm.value.avatarUrl.trim() || undefined,
      bio: profileForm.value.bio.trim() || undefined,
    })
    profile.value = res.data
    if (userStore.profile) {
      userStore.profile.displayName = res.data.displayName
      userStore.profile.avatarUrl = res.data.avatarUrl
    }
    ElMessage.success('个人资料已更新')
    profileDialogVisible.value = false
  } finally {
    profileSubmitting.value = false
  }
}

const submitPet = async () => {
  const payload = sanitizePayload(petForm.value)
  if (!payload.name) {
    ElMessage.warning('请填写宠物昵称')
    return
  }

  submitting.value = true
  try {
    if (editingPetId.value) {
      await updatePet(editingPetId.value, payload)
      ElMessage.success('宠物信息已更新')
    } else {
      await createPet(payload)
      ElMessage.success('宠物创建成功')
    }
    petDialogVisible.value = false
    await loadPets()
  } finally {
    submitting.value = false
  }
}

const onDeletePet = async (id: number) => {
  await ElMessageBox.confirm('删除宠物会同时删除它的回忆记录，是否继续？', '提示', {
    type: 'warning',
  })
  await deletePet(id)
  ElMessage.success('删除成功')
  if (selectedPetId.value === id) {
    selectedPetId.value = null
    memories.value = []
  }
  await loadPets()
}

const selectPet = async (id: number) => {
  selectedPetId.value = id
  await loadMemories()
}

const goPetDetail = async (id: number) => {
  await router.push(`/pets/${id}`)
}

const openCreateMemoryDialog = () => {
  if (!selectedPetId.value) {
    ElMessage.warning('请先选择一个宠物')
    return
  }
  editingMemoryId.value = null
  resetMemoryForm()
  memoryDialogVisible.value = true
}

const openEditMemoryDialog = (memory: MemoryEntry) => {
  editingMemoryId.value = memory.id
  memoryForm.value = {
    title: memory.title,
    content: memory.content,
    eventDate: memory.eventDate || '',
    location: memory.location || '',
    imageUrl: memory.imageUrl || '',
    videoUrl: memory.videoUrl || '',
    videoCoverUrl: memory.videoCoverUrl || '',
    videoDurationSeconds: memory.videoDurationSeconds,
  }
  memoryDialogVisible.value = true
}

const submitMemory = async () => {
  if (!selectedPetId.value) {
    ElMessage.warning('请先选择一个宠物')
    return
  }
  const payload = sanitizePayload(memoryForm.value)
  if (!payload.title || !payload.content) {
    ElMessage.warning('请填写回忆标题和内容')
    return
  }

  submitting.value = true
  try {
    if (editingMemoryId.value) {
      await updateMemory(editingMemoryId.value, payload)
      ElMessage.success('回忆已更新')
    } else {
      await createMemory(selectedPetId.value, payload)
      ElMessage.success('回忆创建成功')
    }
    memoryDialogVisible.value = false
    await loadMemories()
  } finally {
    submitting.value = false
  }
}

const onDeleteMemory = async (id: number) => {
  await ElMessageBox.confirm('确认删除该回忆吗？', '提示', { type: 'warning' })
  await deleteMemory(id)
  ElMessage.success('删除成功')
  await loadMemories()
}

const copyShareLink = async () => {
  if (isAdmin.value) {
    ElMessage.info('管理员账号仅保留管理中心功能')
    return
  }

  const ownerUsername = userStore.profile?.username || ''
  if (!ownerUsername) {
    ElMessage.warning('未获取到当前用户名，请重新登录后再试')
    return
  }

  let targetPet = pets.value.find((pet) => pet.isPublic)
  if (!targetPet) {
    const candidate = selectedPet.value || pets.value[0]
    if (!candidate) {
      ElMessage.warning('请先创建一个宠物后再分享主页')
      return
    }

    const shouldOpenPublic = await ElMessageBox.confirm(
      `你当前没有公开宠物，是否将“${candidate.name}”设为公开并继续分享？`,
      '开启主页分享',
      {
        type: 'warning',
        confirmButtonText: '设为公开并继续',
        cancelButtonText: '取消',
      },
    )
      .then(() => true)
      .catch(() => false)

    if (!shouldOpenPublic) {
      return
    }

    const res = await updatePet(candidate.id, {
      name: candidate.name,
      species: candidate.species || '',
      breed: candidate.breed || '',
      gender: candidate.gender || '',
      birthDate: candidate.birthDate || '',
      memorialDate: candidate.memorialDate || '',
      avatarUrl: candidate.avatarUrl || '',
      description: candidate.description || '',
      isPublic: true,
    })
    targetPet = res.data
    await loadPets()
  }

  const query = targetPet?.shareToken ? `?pet=${encodeURIComponent(targetPet.shareToken)}` : ''
  const link = `${window.location.origin}/home/${encodeURIComponent(ownerUsername)}${query}`

  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success('主页链接已复制')
  } catch {
    window.prompt('自动复制失败，请手动复制下面链接：', link)
    ElMessage.success('已生成主页链接')
  }
}

const logout = async () => {
  userStore.logout()
  await router.push('/login')
}

const goCommunity = async () => {
  await router.push('/community')
}

const goAdminCenter = async () => {
  await router.push('/admin')
}

onMounted(async () => {
  await userStore.refreshProfile()
  if (isAdmin.value) {
    await router.replace('/admin')
    return
  }
  await Promise.all([loadPets(), loadFollowingFeed(), loadProfile()])
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding: 20px;
  background:
    radial-gradient(circle at 0% 0%, rgba(255, 227, 198, 0.58) 0%, rgba(255, 227, 198, 0) 34%),
    radial-gradient(circle at 100% 10%, rgba(184, 223, 255, 0.42) 0%, rgba(184, 223, 255, 0) 32%),
    linear-gradient(180deg, #fff9f2 0%, #f5fafe 48%, #ffffff 100%);
}

.hero {
  margin-bottom: 18px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid #e5ecf8;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 32px rgba(43, 66, 106, 0.08);
}

.hero-title-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-copy {
  max-width: 620px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.16em;
  color: #6b7b94;
  margin-bottom: 8px;
}

.hero-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.hero h1 {
  margin-bottom: 8px;
  font-size: 34px;
  color: #243049;
  line-height: 1.2;
}

.hero p {
  color: #60708a;
}

.login-user {
  margin-top: 10px;
  font-size: 14px;
}

.avatar-trigger {
  border: 1px solid #e2e8f6;
  background: #fff;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  cursor: pointer;
  color: #2f3950;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #dce5f5;
}

.user-center-panel {
  display: grid;
  gap: 10px;
}

.user-center-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-center-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #dce5f5;
}

.user-center-head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6d7587;
}

.user-center-bio {
  color: #55617a;
  font-size: 13px;
  line-height: 1.5;
}

.user-center-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #5d6a82;
}

.user-center-actions {
  display: flex;
  gap: 8px;
}

.panel {
  margin-bottom: 20px;
  border-radius: 14px;
  border: 1px solid #e9eef8;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pet-list {
  display: grid;
  gap: 10px;
}

.pet-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #eceff5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pet-item-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pet-item-avatar {
  width: 52px;
  height: 52px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid #e6ebf5;
}

.pet-item:hover {
  border-color: #c9d6ff;
  background: #f6f9ff;
}

.pet-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.pet-item h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.pet-item p {
  margin: 0;
  color: #7a8393;
  font-size: 13px;
}

.pet-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.selected-pet {
  margin-bottom: 14px;
  color: #606978;
}

.following-feed-list {
  display: grid;
  gap: 10px;
}

.following-dialog-header {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #62718a;
}

.following-feed-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid #e6edf9;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
}

.following-feed-item p {
  margin-top: 6px;
  color: #6f7889;
  font-size: 13px;
}

.following-feed-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  white-space: nowrap;
  color: #7a8393;
  font-size: 12px;
}

.memory-card {
  border-radius: 10px;
}

.memory-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.memory-title-row h3 {
  margin: 0;
  font-size: 17px;
}

.memory-meta {
  margin-top: 8px;
  color: #7a8393;
  font-size: 13px;
}

.memory-image {
  margin-top: 10px;
  width: min(360px, 100%);
  border-radius: 10px;
  border: 1px solid #edf0f8;
}

.memory-video-wrapper {
  margin-top: 10px;
  position: relative;
  width: min(420px, 100%);
}

.memory-video {
  width: 100%;
  max-height: 280px;
  border-radius: 10px;
  background: #000;
}

.video-duration-chip {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  color: #fff;
  background: rgba(0, 0, 0, 0.66);
  font-size: 12px;
}

.upload-row {
  width: 100%;
  display: flex;
  gap: 8px;
}

.upload-row :deep(.el-input) {
  flex: 1;
}

@media (max-width: 992px) {
  .home-page {
    padding: 16px;
  }

  .hero h1 {
    font-size: 26px;
  }

  .hero-title-row {
    flex-direction: column;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .user-center-actions {
    flex-wrap: wrap;
  }
}
</style>
