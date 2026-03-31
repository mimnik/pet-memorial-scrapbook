<template>
  <div class="share-page">
    <div class="top-bar">
      <h1>宠物公开纪念页</h1>
      <router-link to="/login">返回登录</router-link>
    </div>

    <el-card v-if="data" class="pet-card">
      <div class="pet-header">
        <img v-if="data.pet.avatarUrl" :src="data.pet.avatarUrl" alt="avatar" class="pet-avatar" />
        <div>
          <h2>{{ data.pet.name }}</h2>
          <p>{{ [data.pet.species, data.pet.breed].filter(Boolean).join(' / ') || '未知物种' }}</p>
          <p v-if="data.pet.description">{{ data.pet.description }}</p>
        </div>
      </div>
    </el-card>

    <el-empty v-if="!loading && !data" description="未找到可公开访问的宠物纪念页" />

    <el-timeline v-if="data?.memories?.length">
      <el-timeline-item
        v-for="memory in data.memories"
        :key="memory.id"
        :timestamp="memory.eventDate || '未填写日期'"
        placement="top"
      >
        <el-card>
          <h3>{{ memory.title }}</h3>
          <p>{{ memory.content }}</p>
          <p v-if="memory.location">地点：{{ memory.location }}</p>
          <img v-if="memory.imageUrl" :src="memory.imageUrl" alt="memory" class="memory-image" />
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

    <el-empty v-else-if="data && !loading" description="暂无公开回忆" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicPetByToken } from '@/api/public'
import type { PublicPetView } from '@/types/public'

const route = useRoute()
const loading = ref(false)
const data = ref<PublicPetView | null>(null)

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

onMounted(async () => {
  const token = String(route.params.token || '')
  if (!token) return

  loading.value = true
  try {
    const res = await getPublicPetByToken(token)
    data.value = res.data
  } catch {
    ElMessage.error('公开页面加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.share-page {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(180deg, #fef6eb 0%, #f6fbff 100%);
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pet-card {
  margin-bottom: 16px;
}

.pet-header {
  display: flex;
  gap: 14px;
}

.pet-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e8ecf8;
}

.memory-image {
  margin-top: 10px;
  max-width: 100%;
  border-radius: 8px;
}

.memory-video-wrap {
  margin-top: 10px;
  width: min(420px, 100%);
  position: relative;
}

.memory-video {
  width: 100%;
  max-height: 280px;
  border-radius: 8px;
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
</style>
