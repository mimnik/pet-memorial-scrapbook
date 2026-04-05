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
        <el-descriptions-item label="生日">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.birthDate) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="纪念日">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.memorialDate) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="年龄">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayAutoAge(pet.birthDate) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="体重">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.weight) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="婚姻状况">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.maritalStatus) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="技能">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.skills) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="饮食习惯">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.dietaryHabits) }}
          </button>
        </el-descriptions-item>
        <el-descriptions-item label="身体状况">
          <button type="button" class="editable-value" @click="openPetInfoDialog">
            {{ displayEditableValue(pet.physicalCondition) }}
          </button>
        </el-descriptions-item>
      </el-descriptions>
      <p class="pet-edit-tip">提示：点击任意字段可直接进入修改。</p>
    </el-card>

    <el-card class="archive-card" shadow="hover">
      <template #header>
        <div class="archive-header">
          <span>宠物档案（大事记）</span>
          <el-button type="primary" @click="openCreateArchiveDialog">新增档案</el-button>
        </div>
      </template>

      <el-empty v-if="archives.length === 0" description="暂无宠物档案，先记录一条大事吧" />

      <div v-else class="archive-table-wrap">
        <el-table :data="archives" stripe>
          <el-table-column label="记录日期" width="120">
            <template #default="{ row }">
              {{ row.eventDate || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="类型" width="108">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ formatArchiveType(row.archiveType) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="事项">
            <template #default="{ row }">
              <p class="archive-title">{{ row.title }}</p>
              <p v-if="row.note" class="archive-note">{{ row.note }}</p>
            </template>
          </el-table-column>

          <el-table-column label="记录值" width="140">
            <template #default="{ row }">
              {{ formatMetricValue(row.metricValue, row.unit) }}
            </template>
          </el-table-column>

          <el-table-column label="提醒" min-width="180">
            <template #default="{ row }">
              <template v-if="row.reminderEnabled">
                <el-tag :type="row.reminderStatus === 'COMPLETED' ? 'success' : 'warning'" size="small">
                  {{ row.reminderStatus === 'COMPLETED' ? '已完成' : '待提醒' }}
                </el-tag>
                <p class="archive-reminder-time">{{ formatDateTime(row.reminderAt) }}</p>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditArchiveDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="onDeleteArchive(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
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

    <el-dialog v-model="archiveDialogVisible" :title="archiveDialogTitle" width="560px">
      <el-form :model="archiveForm" label-width="96px">
        <el-form-item label="档案类型" required>
          <el-select v-model="archiveForm.archiveType" placeholder="请选择档案类型">
            <el-option
              v-for="option in archiveTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="事项名称" required>
          <el-input v-model="archiveForm.title" maxlength="120" placeholder="例如：年度体检、狂犬疫苗" />
        </el-form-item>

        <el-form-item label="记录值">
          <div class="metric-row">
            <el-input v-model="archiveForm.metricValue" maxlength="100" placeholder="例如：4.8" />
            <el-select v-model="archiveForm.unit" placeholder="单位" clearable>
              <el-option v-for="unit in unitOptions" :key="unit" :label="unit" :value="unit" />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label="记录日期">
          <el-date-picker v-model="archiveForm.eventDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="archiveForm.note" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>

        <el-form-item label="开启提醒">
          <el-switch v-model="archiveForm.reminderEnabled" @change="onReminderToggle" />
        </el-form-item>

        <el-form-item v-if="archiveForm.reminderEnabled" label="提醒时间" required>
          <el-date-picker
            v-model="archiveForm.reminderAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择提醒日期和时间"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="archiveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="archiveSubmitting" @click="submitArchive">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="petInfoDialogVisible" title="编辑宠物详情" width="560px">
      <el-form :model="petInfoForm" label-width="96px">
        <el-form-item label="生日">
          <el-date-picker v-model="petInfoForm.birthDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="纪念日">
          <el-date-picker v-model="petInfoForm.memorialDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="年龄">
          <el-input :model-value="displayAutoAge(petInfoForm.birthDate)" disabled />
        </el-form-item>

        <el-form-item label="体重">
          <el-input v-model="petInfoForm.weight" maxlength="20" placeholder="例如：4.8kg" />
        </el-form-item>

        <el-form-item label="婚姻状况">
          <el-select v-model="petInfoForm.maritalStatus" placeholder="请选择婚姻状况" clearable>
            <el-option
              v-for="option in maritalStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="技能">
          <el-input v-model="petInfoForm.skills" maxlength="500" placeholder="例如：接飞盘、握手" />
        </el-form-item>

        <el-form-item label="饮食习惯">
          <el-input v-model="petInfoForm.dietaryHabits" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>

        <el-form-item label="身体状况">
          <el-input v-model="petInfoForm.physicalCondition" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="petInfoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="petInfoSubmitting" @click="submitPetInfo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPetById, updatePet } from '@/api/pet'
import { listMemoriesByPet } from '@/api/memory'
import { createPetArchive, deletePetArchive, listPetArchivesByPet, updatePetArchive } from '@/api/archive'
import type { Pet } from '@/types/pet'
import type { MemoryEntry } from '@/types/memory'
import type { PetArchiveRecord, PetArchiveRecordPayload } from '@/types/archive'

const route = useRoute()
const router = useRouter()

const pet = ref<Pet | null>(null)
const memories = ref<MemoryEntry[]>([])
const archives = ref<PetArchiveRecord[]>([])
const archiveDialogVisible = ref(false)
const archiveSubmitting = ref(false)
const editingArchiveId = ref<number | null>(null)
const petInfoDialogVisible = ref(false)
const petInfoSubmitting = ref(false)

const maritalStatusOptions = [
  { label: '未婚', value: '未婚' },
  { label: '已婚', value: '已婚' },
  { label: '离异', value: '离异' },
  { label: '丧偶', value: '丧偶' },
  { label: '未知', value: '未知' },
]

const petInfoForm = ref({
  birthDate: '',
  memorialDate: '',
  weight: '',
  maritalStatus: '',
  skills: '',
  dietaryHabits: '',
  physicalCondition: '',
})

const archiveTypeOptions = [
  { label: '疫苗', value: 'VACCINE' },
  { label: '体检', value: 'CHECKUP' },
  { label: '驱虫', value: 'DEWORMING' },
  { label: '用药', value: 'MEDICATION' },
  { label: '其他', value: 'OTHER' },
]

const unitOptions = ['kg', 'g', 'ml', 'mg', '次', '片', '支']

const archiveForm = ref<PetArchiveRecordPayload>({
  archiveType: 'VACCINE',
  title: '',
  metricValue: '',
  unit: '',
  eventDate: new Date().toISOString().slice(0, 10),
  note: '',
  reminderEnabled: false,
  reminderAt: '',
})

const archiveDialogTitle = computed(() => (editingArchiveId.value ? '编辑宠物档案' : '新增宠物档案'))

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

const formatMetricValue = (metricValue?: string, unit?: string) => {
  if (!metricValue) {
    return '-'
  }
  return `${metricValue}${unit || ''}`
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

const displayEditableValue = (value?: string | number | null) => {
  if (value === null || value === undefined) {
    return '点击填写'
  }
  const text = String(value).trim()
  return text ? text : '点击填写'
}

const calculateAgeByBirthDate = (birthDate?: string) => {
  if (!birthDate) {
    return null
  }
  const parts = birthDate.split('-').map((item) => Number(item))
  if (parts.length < 3 || parts.some((item) => !Number.isFinite(item))) {
    return null
  }
  const [year, month, day] = parts
  if (!year || !month || !day) {
    return null
  }
  const today = new Date()
  let age = today.getFullYear() - year
  const currentMonth = today.getMonth() + 1
  const currentDay = today.getDate()
  if (currentMonth < month || (currentMonth === month && currentDay < day)) {
    age -= 1
  }
  return Math.max(age, 0)
}

const displayAutoAge = (birthDate?: string) => {
  const age = calculateAgeByBirthDate(birthDate)
  return age === null ? '点击填写生日自动生成' : `${age}岁`
}

const syncPetInfoForm = (value: Pet) => {
  petInfoForm.value = {
    birthDate: value.birthDate || '',
    memorialDate: value.memorialDate || '',
    weight: value.weight || '',
    maritalStatus: value.maritalStatus || '',
    skills: value.skills || '',
    dietaryHabits: value.dietaryHabits || '',
    physicalCondition: value.physicalCondition || '',
  }
}

const openPetInfoDialog = () => {
  if (!pet.value) {
    return
  }
  syncPetInfoForm(pet.value)
  petInfoDialogVisible.value = true
}

const getCurrentPetId = () => {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    throw new Error('无效的宠物 ID')
  }
  return id
}

const goBack = async () => {
  await router.push('/')
}

const loadArchives = async (petId: number) => {
  const res = await listPetArchivesByPet(petId)
  archives.value = res.data
}

const loadData = async () => {
  try {
    const id = getCurrentPetId()
    const [petRes, memoryRes, archiveRes] = await Promise.all([
      getPetById(id),
      listMemoriesByPet(id),
      listPetArchivesByPet(id),
    ])
    pet.value = petRes.data
    memories.value = memoryRes.data
    archives.value = archiveRes.data
  } catch {
    ElMessage.error('无效的宠物 ID')
    await goBack()
  }
}

const resetArchiveForm = () => {
  archiveForm.value = {
    archiveType: 'VACCINE',
    title: '',
    metricValue: '',
    unit: '',
    eventDate: new Date().toISOString().slice(0, 10),
    note: '',
    reminderEnabled: false,
    reminderAt: '',
  }
}

const openCreateArchiveDialog = () => {
  editingArchiveId.value = null
  resetArchiveForm()
  archiveDialogVisible.value = true
}

const openEditArchiveDialog = (record: PetArchiveRecord) => {
  const safeArchiveType = record.archiveType === 'WEIGHT' ? 'OTHER' : record.archiveType
  editingArchiveId.value = record.id
  archiveForm.value = {
    archiveType: safeArchiveType,
    title: record.title,
    metricValue: record.metricValue || '',
    unit: record.unit || '',
    eventDate: record.eventDate || '',
    note: record.note || '',
    reminderEnabled: record.reminderEnabled,
    reminderAt: record.reminderAt || '',
  }
  archiveDialogVisible.value = true
}

const onReminderToggle = (value: boolean) => {
  if (!value) {
    archiveForm.value.reminderAt = ''
  }
}

const sanitizeArchivePayload = (payload: PetArchiveRecordPayload): PetArchiveRecordPayload => {
  const title = payload.title.trim()
  const metricValue = payload.metricValue?.trim() || undefined
  const unit = payload.unit?.trim() || undefined
  const note = payload.note?.trim() || undefined
  const reminderEnabled = Boolean(payload.reminderEnabled)

  return {
    archiveType: payload.archiveType,
    title,
    metricValue,
    unit,
    eventDate: payload.eventDate || undefined,
    note,
    reminderEnabled,
    reminderAt: reminderEnabled ? payload.reminderAt || undefined : undefined,
  }
}

const submitArchive = async () => {
  let petId = 0
  try {
    petId = getCurrentPetId()
  } catch {
    ElMessage.error('无效的宠物 ID')
    return
  }

  const payload = sanitizeArchivePayload(archiveForm.value)
  if (!payload.title) {
    ElMessage.warning('请填写事项名称')
    return
  }
  if (payload.reminderEnabled && !payload.reminderAt) {
    ElMessage.warning('开启提醒后请设置提醒时间')
    return
  }

  archiveSubmitting.value = true
  try {
    if (editingArchiveId.value) {
      await updatePetArchive(editingArchiveId.value, payload)
      ElMessage.success('宠物档案已更新')
    } else {
      await createPetArchive(petId, payload)
      ElMessage.success('宠物档案已创建')
    }
    archiveDialogVisible.value = false
    await loadArchives(petId)
  } finally {
    archiveSubmitting.value = false
  }
}

const submitPetInfo = async () => {
  if (!pet.value) {
    return
  }

  petInfoSubmitting.value = true
  try {
    const res = await updatePet(pet.value.id, {
      name: pet.value.name,
      species: pet.value.species || '',
      breed: pet.value.breed || '',
      gender: pet.value.gender || '',
      birthDate: petInfoForm.value.birthDate || '',
      memorialDate: petInfoForm.value.memorialDate || '',
      weight: petInfoForm.value.weight.trim() || '',
      maritalStatus: petInfoForm.value.maritalStatus || '',
      skills: petInfoForm.value.skills.trim() || '',
      dietaryHabits: petInfoForm.value.dietaryHabits.trim() || '',
      physicalCondition: petInfoForm.value.physicalCondition.trim() || '',
      avatarUrl: pet.value.avatarUrl || '',
      description: pet.value.description || '',
      isPublic: pet.value.isPublic,
    })
    pet.value = res.data
    petInfoDialogVisible.value = false
    ElMessage.success('宠物详情已更新')
  } finally {
    petInfoSubmitting.value = false
  }
}

const onDeleteArchive = async (id: number) => {
  let petId = 0
  try {
    petId = getCurrentPetId()
  } catch {
    ElMessage.error('无效的宠物 ID')
    return
  }

  await ElMessageBox.confirm('确认删除这条宠物档案吗？', '提示', {
    type: 'warning',
  })

  await deletePetArchive(id)
  ElMessage.success('删除成功')
  await loadArchives(petId)
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
.archive-card,
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

.editable-value {
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  color: #2f3950;
  cursor: pointer;
}

.editable-value:hover {
  color: #2a7de5;
}

.pet-edit-tip {
  margin-top: 10px;
  color: #6e7787;
  font-size: 12px;
}

.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.archive-table-wrap {
  overflow-x: auto;
}

.archive-title {
  margin: 0;
  font-weight: 600;
  color: #2f3950;
}

.archive-note {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6e7787;
  white-space: pre-wrap;
}

.archive-reminder-time {
  margin: 6px 0 0;
  font-size: 12px;
  color: #6e7787;
}

.metric-row {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 8px;
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
  .pet-header,
  .archive-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-row {
    grid-template-columns: 1fr;
  }
}
</style>
