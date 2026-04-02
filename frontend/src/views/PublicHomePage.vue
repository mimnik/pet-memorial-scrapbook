<template>
  <div class="public-home-page">
    <header class="home-header">
      <div>
        <h1>@{{ homeData?.ownerUsername || ownerUsername }} 的宠物纪念主页</h1>
        <p>可切换查看该用户公开的宠物、回忆和社区动态。</p>
      </div>
      <button type="button" class="back-link" @click="goBackHome">返回主页</button>
    </header>

    <el-empty v-if="!loading && !homeData" description="该用户暂无公开宠物主页" />

    <template v-if="homeData">
      <el-card class="pet-switch-card" shadow="hover">
        <div class="pet-switch-list">
          <button
            v-for="pet in homeData.publicPets"
            :key="pet.id"
            class="pet-switch-item"
            :class="{ active: homeData.activePet?.id === pet.id }"
            @click="switchPet(pet.shareToken)"
          >
            <img v-if="pet.avatarUrl" :src="pet.avatarUrl" alt="avatar" class="pet-switch-avatar" />
            <span>{{ pet.name }}</span>
          </button>
        </div>
      </el-card>

      <el-card v-if="homeData.activePet" class="pet-profile-card" shadow="hover">
        <div class="pet-profile-head">
          <img
            v-if="homeData.activePet.avatarUrl"
            :src="homeData.activePet.avatarUrl"
            alt="pet-avatar"
            class="pet-profile-avatar"
          />
          <div>
            <h2>{{ homeData.activePet.name }}</h2>
            <p>
              {{
                [homeData.activePet.species, homeData.activePet.breed, homeData.activePet.gender]
                  .filter(Boolean)
                  .join(' / ') || '未填写基础信息'
              }}
            </p>
            <p v-if="homeData.activePet.description" class="pet-description">{{ homeData.activePet.description }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="timeline-card" shadow="hover">
        <template #header>
          <div class="timeline-title">公开回忆</div>
        </template>

        <el-empty v-if="homeData.memories.length === 0" description="该宠物暂未公开回忆" />

        <el-timeline v-else>
          <el-timeline-item
            v-for="memory in homeData.memories"
            :key="memory.id"
            :timestamp="memory.eventDate || '未填写日期'"
            placement="top"
          >
            <el-card>
              <h3>{{ memory.title }}</h3>
              <p>{{ memory.content }}</p>
              <p v-if="memory.location" class="memory-meta">地点：{{ memory.location }}</p>

              <img v-if="memory.imageUrl" :src="memory.imageUrl" alt="memory-image" class="memory-image" />

              <div v-if="memory.videoUrl" class="memory-video-wrap">
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

      <el-card class="timeline-card" shadow="hover">
        <template #header>
          <div class="timeline-title">公开动态</div>
        </template>

        <el-empty v-if="homeData.communityPosts.length === 0" description="该用户暂未发布社区动态" />

        <div v-else class="public-post-list">
          <div v-for="post in homeData.communityPosts" :key="post.id" class="public-post-item">
            <div class="public-post-head">
              <strong>{{ post.title }}</strong>
              <span>{{ formatPostTime(post.createdAt) }}</span>
            </div>

            <p class="public-post-meta">
              <span>{{ formatMode(post.narrativeMode) }}</span>
              <span> · {{ formatMood(post.moodTag) }}</span>
              <span v-if="post.topicName"> · #{{ post.topicName }}</span>
              <span> · {{ post.petName }}</span>
            </p>

            <p class="public-post-content">{{ post.content }}</p>

            <el-image
              v-if="post.imageUrl"
              :src="post.imageUrl"
              :preview-src-list="[post.imageUrl]"
              preview-teleported
              fit="cover"
              class="memory-image"
            />

            <div v-if="post.videoUrl" class="memory-video-wrap">
              <video
                class="memory-video"
                controls
                preload="metadata"
                :poster="post.videoCoverUrl || undefined"
                :src="post.videoUrl"
              />
              <span v-if="post.videoDurationSeconds" class="video-duration-chip">
                {{ formatDuration(post.videoDurationSeconds) }}
              </span>
            </div>

            <div class="public-post-stats">点赞 {{ post.likeCount }} · 评论 {{ post.commentCount }}</div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserPublicHome } from '@/api/public'
import type { UserPublicHome } from '@/types/public'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const homeData = ref<UserPublicHome | null>(null)

const ownerUsername = computed(() => String(route.params.ownerUsername || ''))
const activePetToken = computed(() => {
  const pet = route.query.pet
  return typeof pet === 'string' ? pet : ''
})

const moodText: Record<string, string> = {
  SUNNY: '晴朗',
  CLOUDY: '多云',
  RAINY: '小雨',
  STORMY: '雷暴',
  RAINBOW: '彩虹',
}

const modeText: Record<string, string> = {
  DAILY: '日常',
  MEMORIAL: '纪念',
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

const formatMood = (mood: string) => moodText[mood] || mood
const formatMode = (mode: string) => modeText[mode] || mode

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

const loadUserHome = async () => {
  if (!ownerUsername.value) {
    homeData.value = null
    return
  }

  loading.value = true
  try {
    const res = await getUserPublicHome(ownerUsername.value, activePetToken.value || undefined)
    homeData.value = res.data
  } catch {
    homeData.value = null
    ElMessage.error('公开主页加载失败')
  } finally {
    loading.value = false
  }
}

const switchPet = async (shareToken: string) => {
  await router.replace({
    path: `/home/${encodeURIComponent(ownerUsername.value)}`,
    query: {
      pet: shareToken,
    },
  })
}

const goBackHome = async () => {
  if (getToken()) {
    await router.push('/')
    return
  }

  if (window.history.length > 1) {
    router.back()
    return
  }

  await router.push('/login')
}

watch(
  () => [ownerUsername.value, activePetToken.value],
  async () => {
    await loadUserHome()
  },
)

onMounted(async () => {
  await loadUserHome()
})
</script>

<style scoped>
.public-home-page {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(180deg, #fff8ef 0%, #f3fbff 45%, #ffffff 100%);
}

.home-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.home-header h1 {
  margin: 0 0 8px;
  font-size: 30px;
}

.home-header p {
  margin: 0;
  color: #6e7787;
}

.back-link {
  border: none;
  background: transparent;
  color: #3576da;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
}

.pet-switch-card,
.pet-profile-card,
.timeline-card {
  border-radius: 14px;
  margin-bottom: 14px;
}

.pet-switch-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pet-switch-item {
  border: 1px solid #e4e9f5;
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px 6px 6px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.pet-switch-item.active {
  border-color: #4f87de;
  background: #eef5ff;
}

.pet-switch-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.pet-profile-head {
  display: flex;
  gap: 12px;
}

.pet-profile-avatar {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e4e9f5;
}

.pet-profile-head h2 {
  margin: 0 0 8px;
}

.pet-profile-head p {
  margin: 0 0 4px;
  color: #667083;
}

.pet-description {
  color: #3d4553;
}

.timeline-title {
  font-weight: 600;
}

.memory-meta {
  color: #6e7787;
  font-size: 13px;
}

.memory-image {
  margin-top: 10px;
  width: min(360px, 100%);
  border-radius: 10px;
  border: 1px solid #eef1f8;
  cursor: zoom-in;
}

.memory-image :deep(img) {
  border-radius: 10px;
}

.memory-video-wrap {
  margin-top: 10px;
  width: min(420px, 100%);
  position: relative;
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

.public-post-list {
  display: grid;
  gap: 12px;
}

.public-post-item {
  border: 1px solid #e8edf8;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.public-post-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.public-post-head span {
  color: #6e7787;
  font-size: 12px;
}

.public-post-meta {
  margin-top: 8px;
  color: #6e7787;
  font-size: 12px;
}

.public-post-content {
  margin-top: 8px;
  color: #3d4553;
  white-space: pre-wrap;
}

.public-post-stats {
  margin-top: 8px;
  color: #6e7787;
  font-size: 12px;
}

@media (max-width: 768px) {
  .public-home-page {
    padding: 14px;
  }

  .home-header {
    flex-direction: column;
  }
}
</style>
