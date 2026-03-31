<template>
  <div class="pet-detail-page">
    <el-page-header @back="goBack" content="宠物详情" />

    <el-card v-if="pet" class="pet-card" shadow="hover">
      <div class="pet-header">
        <div class="pet-title-wrap">
          <img v-if="pet.avatarUrl" :src="pet.avatarUrl" alt="pet-avatar" class="pet-avatar" />
          <div>
            <h2>{{ pet.name }}</h2>
            <p>{{ [pet.species, pet.breed, pet.gender].filter(Boolean).join(' / ') || '未填写基础信息' }}</p>
          </div>
        </div>
        <el-tag :type="pet.isPublic ? 'success' : 'info'">
          {{ pet.isPublic ? '公开中' : '私密' }}
        </el-tag>
      </div>
      <p v-if="pet.description" class="description">{{ pet.description }}</p>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="生日">{{ pet.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="纪念日">{{ pet.memorialDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ pet.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ pet.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="timeline-card" shadow="hover">
      <template #header>
        <div class="timeline-header">
          <span>回忆日记时间线</span>
          <el-button type="primary" plain @click="goBack">返回主页继续编辑</el-button>
        </div>
      </template>

      <el-empty v-if="memories.length === 0" description="暂无日记记录" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="memory in memories"
          :key="memory.id"
          :timestamp="memory.eventDate || '未填写日期'"
          placement="top"
        >
          <el-card>
            <h3>{{ memory.title }}</h3>
            <p class="content">{{ memory.content }}</p>
            <p v-if="memory.location" class="meta">地点：{{ memory.location }}</p>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPetById } from '@/api/pet'
import { listMemoriesByPet } from '@/api/memory'
import type { Pet } from '@/types/pet'
import type { MemoryEntry } from '@/types/memory'

const route = useRoute()
const router = useRouter()

const pet = ref<Pet | null>(null)
const memories = ref<MemoryEntry[]>([])

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

const goBack = async () => {
  await router.push('/')
}

const loadData = async () => {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    ElMessage.error('无效的宠物 ID')
    await goBack()
    return
  }

  const [petRes, memoryRes] = await Promise.all([getPetById(id), listMemoriesByPet(id)])
  pet.value = petRes.data
  memories.value = memoryRes.data
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.pet-detail-page {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(180deg, #fff8f0 0%, #f4fbff 45%, #ffffff 100%);
}

.pet-card,
.timeline-card {
  margin-top: 16px;
  border-radius: 14px;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.pet-title-wrap {
  display: flex;
  gap: 12px;
}

.pet-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e6ecf8;
}

.pet-header h2 {
  margin: 0 0 6px;
}

.pet-header p {
  margin: 0;
  color: #6e7787;
}

.description {
  margin-bottom: 12px;
  color: #333a46;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content {
  margin: 8px 0;
  white-space: pre-wrap;
}

.meta {
  color: #6e7787;
  font-size: 13px;
}

.memory-image {
  margin-top: 10px;
  width: min(360px, 100%);
  border-radius: 10px;
  border: 1px solid #eef1f8;
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

@media (max-width: 768px) {
  .pet-detail-page {
    padding: 14px;
  }

  .timeline-header,
  .pet-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
