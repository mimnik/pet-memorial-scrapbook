<template>
  <div class="community-page">
    <header class="community-header">
      <div>
        <h1>宠物微社区</h1>
        <p>支持搜索、个性化推荐和宠物热榜，用更有温度的方式记录陪伴。</p>
      </div>
      <div class="header-actions">
        <el-button @click="goHome">返回主页</el-button>
        <el-button type="primary" @click="openPostDialog">发布动态</el-button>
        <el-button @click="openMessageDialog()">我的私信</el-button>
        <el-button @click="openMyReportsDialog">我的举报</el-button>
        <el-button v-if="isAdmin" type="warning" plain @click="openAdminReportsDialog">举报处理</el-button>
        <el-button type="success" plain @click="loadFollowing">只看关注</el-button>
        <el-button type="info" plain @click="loadMine">只看我的</el-button>
      </div>
    </header>

    <el-card class="feed-filter" shadow="hover">
      <div class="filter-row">
        <el-input
          v-model="activeKeyword"
          placeholder="搜索标题 / 正文 / 宠物名 / 用户名"
          clearable
          @keyup.enter="onFilterChange"
        />
        <el-select v-model="activeMode" clearable placeholder="叙事时态" @change="onFilterChange">
          <el-option label="日常" value="DAILY" />
          <el-option label="纪念" value="MEMORIAL" />
        </el-select>
        <el-select v-model="activeMood" clearable placeholder="情绪天气" @change="onFilterChange">
          <el-option label="晴朗" value="SUNNY" />
          <el-option label="多云" value="CLOUDY" />
          <el-option label="小雨" value="RAINY" />
          <el-option label="雷暴" value="STORMY" />
          <el-option label="彩虹" value="RAINBOW" />
        </el-select>
        <el-select v-model="activeTopicId" clearable placeholder="话题" @change="onFilterChange">
          <el-option v-for="topic in topics" :key="topic.id" :label="topic.name" :value="topic.id" />
        </el-select>
        <el-button type="primary" @click="onFilterChange">搜索</el-button>
        <el-button @click="loadAll">重置</el-button>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-empty v-if="posts.length === 0" description="暂无动态，试试发布第一条或调整筛选条件" />

        <div v-else class="feed-list">
          <el-card v-for="post in posts" :key="post.id" class="feed-item" shadow="hover" :data-post-id="post.id">
            <div class="feed-top">
              <div class="feed-author-wrap">
                <img v-if="post.petAvatarUrl" :src="post.petAvatarUrl" alt="pet-avatar" class="pet-mini-avatar" />
                <div>
                  <h3>{{ post.title }}</h3>
                  <p class="feed-meta">
                    <span>@{{ post.authorUsername }}</span>
                    <span> · {{ post.petName }}</span>
                    <span> · {{ formatMode(post.narrativeMode) }}</span>
                    <span> · {{ formatMood(post.moodTag) }}</span>
                  </p>
                </div>
              </div>
              <div class="feed-tags">
                <el-tag v-if="post.topicName" type="primary" size="small">#{{ post.topicName }}</el-tag>
                <el-tag v-if="post.petVoice" type="warning" size="small">宠物视角</el-tag>
                <el-tag v-if="post.relayEnabled" type="success" size="small">接力开启</el-tag>
                <el-tag v-if="post.recommendationScore !== undefined" type="info" size="small">
                  推荐值 {{ post.recommendationScore }}
                </el-tag>
              </div>
            </div>

            <p class="feed-content">{{ post.content }}</p>

            <el-image
              v-if="post.imageUrl"
              :src="post.imageUrl"
              :preview-src-list="[post.imageUrl]"
              preview-teleported
              fit="cover"
              class="feed-image"
            />

            <div v-if="post.videoUrl" class="feed-video-wrap">
              <video
                class="feed-video"
                controls
                preload="metadata"
                :poster="post.videoCoverUrl || undefined"
                :src="post.videoUrl"
              />
              <span v-if="post.videoDurationSeconds" class="video-duration-chip">
                {{ formatDuration(post.videoDurationSeconds) }}
              </span>
            </div>

            <div class="feed-actions">
              <el-button link @click="onToggleLike(post)">
                {{ post.likedByMe ? '取消赞' : '点赞' }} ({{ post.likeCount }})
              </el-button>
              <el-button link @click="openCommentDialog(post)">评论 ({{ post.commentCount }})</el-button>
              <el-button
                v-if="post.authorUsername !== currentUsername"
                link
                type="primary"
                @click="onToggleFollow(post)"
              >
                {{ post.authorFollowedByMe ? '取消关注' : '关注作者' }}
              </el-button>
              <el-button v-if="post.authorUsername !== currentUsername" link @click="openMessageDialog(post.authorUsername)">
                私信
              </el-button>
              <el-button link type="primary" @click="goUserHome(post.authorUsername)">查看主页</el-button>
              <el-button link type="danger" @click="openReportDialog(post)">举报</el-button>
            </div>
          </el-card>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="side-card" shadow="hover">
          <template #header>
            <span>搜索用户主页</span>
          </template>
          <div class="user-search-row">
            <el-input
              v-model="userSearchKeyword"
              clearable
              placeholder="输入用户名或昵称"
              @keyup.enter="onSearchPublicUsers"
            />
            <el-button type="primary" :loading="userSearchLoading" @click="onSearchPublicUsers">搜索</el-button>
          </div>

          <el-empty
            v-if="!userSearchLoading && userSearchKeyword.trim() && userSearchResults.length === 0"
            description="未找到可公开访问的用户"
          />

          <div v-if="userSearchResults.length > 0" class="user-search-list" v-loading="userSearchLoading">
            <button
              v-for="user in userSearchResults"
              :key="user.username"
              class="user-search-item"
              @click="goUserHome(user.username)"
            >
              <img :src="user.avatarUrl || defaultAvatarUrl" alt="user-avatar" class="user-search-avatar" />
              <div>
                <strong>{{ user.displayName || `@${user.username}` }}</strong>
                <p>@{{ user.username }} · 公开宠物 {{ user.publicPetCount }} · 动态 {{ user.recentPostCount }}</p>
              </div>
            </button>
          </div>
        </el-card>

        <el-card class="side-card" shadow="hover">
          <template #header>
            <span>推荐内容</span>
          </template>
          <el-empty v-if="recommendPosts.length === 0" description="暂无推荐" />
          <div v-else class="recommend-list">
            <button
              v-for="post in recommendPosts"
              :key="post.id"
              class="recommend-item"
              @click="scrollToPost(post.id)"
            >
              <strong>{{ post.title }}</strong>
              <span>@{{ post.authorUsername }} · {{ post.petName }}</span>
              <span class="recommend-score">推荐值 {{ post.recommendationScore || 0 }}</span>
            </button>
          </div>
        </el-card>

        <el-card class="side-card" shadow="hover">
          <template #header>
            <span>宠物热榜</span>
          </template>
          <el-empty v-if="hotPets.length === 0" description="暂无热榜数据" />
          <ol v-else class="hot-rank-list">
            <li
              v-for="(item, index) in hotPets"
              :key="item.petId"
              class="hot-rank-item"
              @click="goHotPetHome(item)"
            >
              <div class="hot-left">
                <span class="hot-index">{{ index + 1 }}</span>
                <img v-if="item.petAvatarUrl" :src="item.petAvatarUrl" alt="pet-avatar" class="hot-avatar" />
                <div>
                  <strong>{{ item.petName }}</strong>
                  <p>@{{ item.ownerUsername }}</p>
                </div>
              </div>
              <div class="hot-right">
                <span>{{ item.heatScore.toFixed(1) }}</span>
                <small>{{ item.postCount }} 帖</small>
                <el-button link type="primary" @click.stop="goHotPetHome(item)">查看主页</el-button>
              </div>
            </li>
          </ol>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="postDialogVisible" title="发布新动态" width="720px">
      <el-form :model="postForm" label-width="96px">
        <el-form-item label="选择宠物" required>
          <el-select v-model="postForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in pets" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" required>
          <el-input v-model="postForm.title" maxlength="200" />
        </el-form-item>

        <el-form-item label="正文" required>
          <el-input v-model="postForm.content" type="textarea" :rows="4" maxlength="5000" />
        </el-form-item>

        <el-form-item label="所属话题">
          <div class="topic-select-row">
            <el-select v-model="postForm.topicId" clearable placeholder="可选，发布到指定话题" style="width: 100%">
              <el-option v-for="topic in topics" :key="topic.id" :label="topic.name" :value="topic.id" />
            </el-select>
            <el-button @click="openTopicDialog">新建话题</el-button>
          </div>
        </el-form-item>

        <el-form-item label="图片链接">
          <div class="upload-row">
            <el-input v-model="postForm.imageUrl" placeholder="上传后自动填写或手动输入" />
            <el-upload :show-file-list="false" :http-request="uploadPostImage" accept="image/*">
              <el-button>上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="视频链接">
          <div class="upload-row">
            <el-input v-model="postForm.videoUrl" placeholder="上传后自动填写或手动输入" />
            <el-upload :show-file-list="false" :http-request="uploadPostVideo" accept="video/*">
              <el-button>上传视频</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="视频时长">
          <el-input
            :model-value="postForm.videoDurationSeconds ? formatDuration(postForm.videoDurationSeconds) : ''"
            disabled
            placeholder="上传视频后自动计算"
          />
        </el-form-item>

        <el-form-item label="叙事时态">
          <el-radio-group v-model="postForm.narrativeMode">
            <el-radio-button label="DAILY">日常</el-radio-button>
            <el-radio-button label="MEMORIAL">纪念</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="情绪天气">
          <el-select v-model="postForm.moodTag" style="width: 100%">
            <el-option label="晴朗" value="SUNNY" />
            <el-option label="多云" value="CLOUDY" />
            <el-option label="小雨" value="RAINY" />
            <el-option label="雷暴" value="STORMY" />
            <el-option label="彩虹" value="RAINBOW" />
          </el-select>
        </el-form-item>

        <el-form-item label="创新能力">
          <div class="switch-row">
            <el-switch v-model="postForm.petVoice" active-text="宠物第一视角" />
            <el-switch v-model="postForm.relayEnabled" active-text="纪念接力" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="commentDialogVisible" title="评论与纪念接力" width="640px">
      <el-empty v-if="comments.length === 0" description="还没有评论，留下第一条吧" />
      <div v-else class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div>
            <strong>@{{ comment.authorUsername }}</strong>
            <el-tag v-if="comment.relayReply" type="success" size="small" style="margin-left: 8px">接力</el-tag>
          </div>
          <p>{{ comment.content }}</p>
        </div>
      </div>

      <el-form :model="commentForm" label-width="88px" style="margin-top: 14px">
        <el-form-item label="评论内容" required>
          <el-input v-model="commentForm.content" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>
        <el-form-item label="纪念接力">
          <el-switch v-model="commentForm.relayReply" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="commentDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="commentSubmitting" @click="submitComment">发布评论</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="topicDialogVisible" title="创建话题" width="480px">
      <el-form :model="topicForm" label-width="88px">
        <el-form-item label="话题名称" required>
          <el-input v-model="topicForm.name" maxlength="50" placeholder="例如：春日散步" />
        </el-form-item>
        <el-form-item label="话题描述">
          <el-input v-model="topicForm.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="topicSubmitting" @click="submitTopic">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="messageDialogVisible" title="私信中心" width="860px">
      <div class="message-layout">
        <aside class="conversation-pane">
          <div class="conversation-header">
            <span>会话列表</span>
            <el-button link type="primary" @click="loadConversations">刷新</el-button>
          </div>
          <el-empty v-if="conversations.length === 0" description="暂无会话" />
          <div v-else class="conversation-list">
            <button
              v-for="item in conversations"
              :key="item.peerUsername"
              class="conversation-item"
              :class="{ active: activeChatUsername === item.peerUsername }"
              @click="selectConversation(item.peerUsername)"
            >
              <div>
                <strong>@{{ item.peerUsername }}</strong>
                <p>{{ item.lastMessage }}</p>
              </div>
              <div class="conversation-meta">
                <span v-if="item.unreadCount > 0" class="unread-dot">{{ item.unreadCount }}</span>
              </div>
            </button>
          </div>
        </aside>

        <section class="chat-pane">
          <el-empty v-if="!activeChatUsername" description="请选择或发起一个会话" />
          <template v-else>
            <h3 class="chat-title">与 @{{ activeChatUsername }} 的私信</h3>
            <div class="chat-message-list" v-loading="chatLoading">
              <div v-if="chatMessages.length === 0" class="chat-empty">暂无消息，发一条打个招呼吧。</div>
              <div
                v-for="msg in chatMessages"
                :key="msg.id"
                class="chat-message-item"
                :class="{ mine: msg.fromMe }"
              >
                <p>{{ msg.content }}</p>
              </div>
            </div>
            <div class="chat-send-row">
              <el-input
                v-model="messageForm.content"
                type="textarea"
                :rows="2"
                maxlength="2000"
                placeholder="输入私信内容"
              />
              <el-button type="primary" :loading="messageSubmitting" @click="submitMessage">发送</el-button>
            </div>
          </template>
        </section>
      </div>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="举报内容" width="520px">
      <el-form :model="reportForm" label-width="88px">
        <el-form-item label="举报对象">
          <el-input :model-value="`${reportForm.targetType} #${reportForm.targetId}`" disabled />
        </el-form-item>
        <el-form-item label="举报原因" required>
          <el-input v-model="reportForm.reason" maxlength="120" placeholder="请简要描述举报原因" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="reportForm.details" type="textarea" :rows="3" maxlength="2000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="reportSubmitting" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="myReportsDialogVisible" title="我的举报记录" width="760px">
      <el-empty v-if="myReports.length === 0" description="暂无举报记录" />
      <el-table v-else :data="myReports" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="targetType" label="类型" width="90" />
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column prop="reason" label="原因" min-width="160" />
        <el-table-column prop="status" label="状态" width="110" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="adminReportsDialogVisible" title="举报处理面板" width="860px">
      <div class="admin-report-toolbar">
        <el-select v-model="adminReportStatus" style="width: 160px" @change="loadAdminReports">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已通过" value="RESOLVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button @click="loadAdminReports">刷新</el-button>
      </div>
      <el-empty v-if="adminReports.length === 0" description="暂无数据" />
      <el-table v-else :data="adminReports" size="small" v-loading="adminReportsLoading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterUsername" label="举报人" width="110" />
        <el-table-column prop="targetType" label="类型" width="90" />
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column prop="reason" label="原因" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-space>
              <el-button
                link
                type="success"
                :disabled="scope.row.status !== 'PENDING'"
                :loading="adminHandleLoadingId === scope.row.id"
                @click="onHandleReport(scope.row.id, 'RESOLVED')"
              >
                通过
              </el-button>
              <el-button
                link
                type="danger"
                :disabled="scope.row.status !== 'PENDING'"
                :loading="adminHandleLoadingId === scope.row.id"
                @click="onHandleReport(scope.row.id, 'REJECTED')"
              >
                驳回
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import {
  createCommunityComment,
  createCommunityPost,
  createCommunityTopic,
  listCommunityComments,
  listCommunityFeed,
  listCommunityTopics,
  listFollowingCommunityFeed,
  listCommunityHotPets,
  listCommunityRecommendations,
  listMyCommunityPosts,
  toggleCommunityLike,
} from '@/api/community'
import { followUser, unfollowUser } from '@/api/social'
import { searchPublicUsers } from '@/api/public'
import { createReport, handleReport, listAdminReports, listMyReports } from '@/api/report'
import { listMessageConversations, listMessagesWithUser, sendMessageToUser } from '@/api/message'
import { listPets } from '@/api/pet'
import { uploadImage, uploadMedia } from '@/api/file'
import { useUserStore } from '@/stores/user'
import type {
  CommunityComment,
  CommunityFeedParams,
  CommunityMoodTag,
  CommunityNarrativeMode,
  CommunityPost,
  CommunityTopic,
  PetHotRank,
} from '@/types/community'
import type { MessageConversation, MessageItem } from '@/types/message'
import type { ReportItem, ReportStatus } from '@/types/report'
import type { Pet } from '@/types/pet'
import type { PublicUserSearchItem } from '@/types/public'

const router = useRouter()
const userStore = useUserStore()

const pets = ref<Pet[]>([])
const posts = ref<CommunityPost[]>([])
const recommendPosts = ref<CommunityPost[]>([])
const hotPets = ref<PetHotRank[]>([])
const comments = ref<CommunityComment[]>([])
const topics = ref<CommunityTopic[]>([])
const defaultAvatarUrl = '/default-avatar.jpg'

const userSearchKeyword = ref('')
const userSearchLoading = ref(false)
const userSearchResults = ref<PublicUserSearchItem[]>([])

const activeMode = ref<CommunityNarrativeMode | ''>('')
const activeMood = ref<CommunityMoodTag | ''>('')
const activeKeyword = ref('')
const activeTopicId = ref<number | undefined>(undefined)

const submitting = ref(false)
const commentSubmitting = ref(false)
const topicSubmitting = ref(false)
const messageSubmitting = ref(false)
const reportSubmitting = ref(false)

const postDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const topicDialogVisible = ref(false)
const messageDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const myReportsDialogVisible = ref(false)
const adminReportsDialogVisible = ref(false)
const activeCommentPostId = ref<number | null>(null)
const activeChatUsername = ref('')
const chatLoading = ref(false)
const adminReportsLoading = ref(false)
const adminHandleLoadingId = ref<number | null>(null)

const conversations = ref<MessageConversation[]>([])
const chatMessages = ref<MessageItem[]>([])
const myReports = ref<ReportItem[]>([])
const adminReports = ref<ReportItem[]>([])
const adminReportStatus = ref<ReportStatus | ''>('PENDING')

const postForm = ref({
  petId: 0,
  topicId: undefined as number | undefined,
  title: '',
  content: '',
  imageUrl: '',
  videoUrl: '',
  videoCoverUrl: '',
  videoDurationSeconds: undefined as number | undefined,
  moodTag: 'SUNNY' as CommunityMoodTag,
  narrativeMode: 'DAILY' as CommunityNarrativeMode,
  petVoice: false,
  relayEnabled: false,
})

const commentForm = ref({
  content: '',
  relayReply: false,
})

const topicForm = ref({
  name: '',
  description: '',
})

const messageForm = ref({
  content: '',
})

const reportForm = ref({
  targetType: 'POST' as 'POST',
  targetId: 0,
  reason: '',
  details: '',
})

const currentUsername = computed(() => userStore.profile?.username || '')
const isAdmin = computed(() => userStore.profile?.role === 'ROLE_ADMIN')

const moodText: Record<CommunityMoodTag, string> = {
  SUNNY: '晴朗',
  CLOUDY: '多云',
  RAINY: '小雨',
  STORMY: '雷暴',
  RAINBOW: '彩虹',
}

const modeText: Record<CommunityNarrativeMode, string> = {
  DAILY: '日常',
  MEMORIAL: '纪念',
}

const formatMood = (mood: CommunityMoodTag) => moodText[mood] || mood
const formatMode = (mode: CommunityNarrativeMode) => modeText[mode] || mode

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

const goHome = async () => {
  await router.push('/')
}

const goUserHome = async (username: string, petShareToken?: string) => {
  const target = username.trim()
  if (!target) {
    return
  }

  await router.push({
    path: `/home/${encodeURIComponent(target)}`,
    query: petShareToken ? { pet: petShareToken } : undefined,
  })
}

const onSearchPublicUsers = async () => {
  const keyword = userSearchKeyword.value.trim()
  if (!keyword) {
    userSearchResults.value = []
    return
  }

  userSearchLoading.value = true
  try {
    const res = await searchPublicUsers(keyword, 12)
    userSearchResults.value = res.data
  } finally {
    userSearchLoading.value = false
  }
}

const goHotPetHome = async (item: PetHotRank) => {
  await goUserHome(item.ownerUsername, item.petShareToken)
}

const resetPostForm = () => {
  postForm.value = {
    petId: postForm.value.petId || pets.value[0]?.id || 0,
    topicId: undefined,
    title: '',
    content: '',
    imageUrl: '',
    videoUrl: '',
    videoCoverUrl: '',
    videoDurationSeconds: undefined,
    moodTag: 'SUNNY',
    narrativeMode: 'DAILY',
    petVoice: false,
    relayEnabled: false,
  }
}

const openPostDialog = () => {
  resetPostForm()
  postDialogVisible.value = true
}

const openTopicDialog = () => {
  topicForm.value.name = ''
  topicForm.value.description = ''
  topicDialogVisible.value = true
}

const loadPets = async () => {
  const res = await listPets()
  pets.value = res.data
  if (!postForm.value.petId && pets.value.length > 0) {
    postForm.value.petId = pets.value[0]?.id || 0
  }
}

const loadTopics = async () => {
  const res = await listCommunityTopics()
  topics.value = res.data
}

const loadFeed = async () => {
  const params: CommunityFeedParams = {}
  if (activeMode.value) {
    params.mode = activeMode.value
  }
  if (activeMood.value) {
    params.mood = activeMood.value
  }
  if (activeKeyword.value.trim()) {
    params.keyword = activeKeyword.value.trim()
  }
  if (activeTopicId.value) {
    params.topicId = activeTopicId.value
  }

  const res = await listCommunityFeed(params)
  posts.value = res.data
}

const loadRecommendations = async () => {
  const res = await listCommunityRecommendations(6)
  recommendPosts.value = res.data
}

const loadHotPets = async () => {
  const res = await listCommunityHotPets(10)
  hotPets.value = res.data
}

const loadAll = async () => {
  activeMode.value = ''
  activeMood.value = ''
  activeKeyword.value = ''
  activeTopicId.value = undefined
  await Promise.all([loadFeed(), loadRecommendations(), loadHotPets()])
}

const loadMine = async () => {
  const res = await listMyCommunityPosts()
  posts.value = res.data
}

const loadFollowing = async () => {
  const res = await listFollowingCommunityFeed()
  posts.value = res.data
}

const onFilterChange = async () => {
  await loadFeed()
}

const submitTopic = async () => {
  if (!topicForm.value.name.trim()) {
    ElMessage.warning('请填写话题名称')
    return
  }
  topicSubmitting.value = true
  try {
    const res = await createCommunityTopic({
      name: topicForm.value.name.trim(),
      description: topicForm.value.description.trim() || undefined,
    })
    await loadTopics()
    postForm.value.topicId = res.data.id
    topicDialogVisible.value = false
    ElMessage.success('话题创建成功')
  } finally {
    topicSubmitting.value = false
  }
}

const uploadPostImage = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const res = await uploadImage(file)
  postForm.value.imageUrl = res.data.url
  ElMessage.success('图片上传成功')
}

const uploadPostVideo = async (options: UploadRequestOptions) => {
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

  postForm.value.videoUrl = uploadRes.data.url
  postForm.value.videoDurationSeconds = duration
  if (coverBlob) {
    const coverRes = await uploadImage(coverBlob, `${Date.now()}-post-cover.jpg`)
    postForm.value.videoCoverUrl = coverRes.data.url
  }

  ElMessage.success('视频上传成功')
}

const submitPost = async () => {
  if (!postForm.value.petId) {
    ElMessage.warning('请先选择宠物')
    return
  }
  if (!postForm.value.title.trim() || !postForm.value.content.trim()) {
    ElMessage.warning('请填写标题和正文')
    return
  }

  submitting.value = true
  try {
    await createCommunityPost({
      petId: postForm.value.petId,
      topicId: postForm.value.topicId,
      title: postForm.value.title.trim(),
      content: postForm.value.content.trim(),
      imageUrl: postForm.value.imageUrl.trim() || undefined,
      videoUrl: postForm.value.videoUrl.trim() || undefined,
      videoCoverUrl: postForm.value.videoCoverUrl.trim() || undefined,
      videoDurationSeconds: postForm.value.videoDurationSeconds,
      moodTag: postForm.value.moodTag,
      narrativeMode: postForm.value.narrativeMode,
      petVoice: postForm.value.petVoice,
      relayEnabled: postForm.value.relayEnabled,
    })
    ElMessage.success('发布成功')
    postDialogVisible.value = false
    resetPostForm()
    await Promise.all([loadFeed(), loadRecommendations(), loadHotPets()])
  } finally {
    submitting.value = false
  }
}

const onToggleLike = async (post: CommunityPost) => {
  const res = await toggleCommunityLike(post.id)
  const updated = res.data
  const idx = posts.value.findIndex((item) => item.id === updated.id)
  if (idx >= 0) {
    posts.value[idx] = updated
  }
  const recommendIdx = recommendPosts.value.findIndex((item) => item.id === updated.id)
  if (recommendIdx >= 0) {
    recommendPosts.value[recommendIdx] = {
      ...recommendPosts.value[recommendIdx],
      ...updated,
    }
  }
}

const onToggleFollow = async (post: CommunityPost) => {
  if (!post.authorUsername || post.authorUsername === currentUsername.value) {
    return
  }

  const following = !!post.authorFollowedByMe
  if (following) {
    await unfollowUser(post.authorUsername)
    ElMessage.success('已取消关注')
  } else {
    await followUser(post.authorUsername)
    ElMessage.success('关注成功')
  }

  const next = !following
  posts.value = posts.value.map((item) =>
    item.authorUsername === post.authorUsername ? { ...item, authorFollowedByMe: next } : item,
  )
  recommendPosts.value = recommendPosts.value.map((item) =>
    item.authorUsername === post.authorUsername ? { ...item, authorFollowedByMe: next } : item,
  )
}

const openCommentDialog = async (post: CommunityPost) => {
  activeCommentPostId.value = post.id
  commentDialogVisible.value = true
  commentForm.value.content = ''
  commentForm.value.relayReply = false
  const res = await listCommunityComments(post.id)
  comments.value = res.data
}

const submitComment = async () => {
  if (!activeCommentPostId.value) {
    return
  }
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请填写评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    await createCommunityComment(activeCommentPostId.value, {
      content: commentForm.value.content.trim(),
      relayReply: commentForm.value.relayReply,
    })
    ElMessage.success('评论成功')
    const [commentRes, feedRes] = await Promise.all([
      listCommunityComments(activeCommentPostId.value),
      listCommunityFeed({
        mode: activeMode.value || undefined,
        mood: activeMood.value || undefined,
        keyword: activeKeyword.value.trim() || undefined,
        topicId: activeTopicId.value || undefined,
      }),
    ])
    comments.value = commentRes.data
    posts.value = feedRes.data
    commentForm.value.content = ''
    commentForm.value.relayReply = false
  } finally {
    commentSubmitting.value = false
  }
}

const scrollToPost = (id: number) => {
  const el = document.querySelector(`[data-post-id="${id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const loadConversations = async () => {
  const res = await listMessageConversations()
  conversations.value = res.data
}

const loadChatMessages = async (username: string) => {
  chatLoading.value = true
  try {
    const res = await listMessagesWithUser(username)
    chatMessages.value = res.data
  } finally {
    chatLoading.value = false
  }
}

const selectConversation = async (username: string) => {
  activeChatUsername.value = username
  await loadChatMessages(username)
  await loadConversations()
}

const openMessageDialog = async (username?: string) => {
  messageDialogVisible.value = true
  await loadConversations()

  if (username) {
    await selectConversation(username)
    return
  }

  if (!activeChatUsername.value && conversations.value.length > 0) {
    const first = conversations.value[0]
    if (first) {
      await selectConversation(first.peerUsername)
    }
  }
}

const submitMessage = async () => {
  if (!activeChatUsername.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  if (!messageForm.value.content.trim()) {
    ElMessage.warning('请输入私信内容')
    return
  }

  messageSubmitting.value = true
  try {
    await sendMessageToUser(activeChatUsername.value, {
      content: messageForm.value.content.trim(),
    })
    messageForm.value.content = ''
    await selectConversation(activeChatUsername.value)
    ElMessage.success('发送成功')
  } finally {
    messageSubmitting.value = false
  }
}

const openReportDialog = (post: CommunityPost) => {
  reportForm.value = {
    targetType: 'POST',
    targetId: post.id,
    reason: '',
    details: '',
  }
  reportDialogVisible.value = true
}

const submitReport = async () => {
  if (!reportForm.value.reason.trim()) {
    ElMessage.warning('请填写举报原因')
    return
  }

  reportSubmitting.value = true
  try {
    await createReport({
      targetType: reportForm.value.targetType,
      targetId: reportForm.value.targetId,
      reason: reportForm.value.reason.trim(),
      details: reportForm.value.details.trim() || undefined,
    })
    ElMessage.success('举报已提交')
    reportDialogVisible.value = false
  } finally {
    reportSubmitting.value = false
  }
}

const loadMyReports = async () => {
  const res = await listMyReports()
  myReports.value = res.data
}

const openMyReportsDialog = async () => {
  myReportsDialogVisible.value = true
  await loadMyReports()
}

const loadAdminReports = async () => {
  if (!isAdmin.value) {
    return
  }

  adminReportsLoading.value = true
  try {
    const res = await listAdminReports((adminReportStatus.value || undefined) as ReportStatus | undefined)
    adminReports.value = res.data
  } finally {
    adminReportsLoading.value = false
  }
}

const openAdminReportsDialog = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('仅管理员可访问')
    return
  }
  adminReportsDialogVisible.value = true
  await loadAdminReports()
}

const onHandleReport = async (id: number, status: Exclude<ReportStatus, 'PENDING'>) => {
  const actionText = status === 'RESOLVED' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${actionText}该举报吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }

  adminHandleLoadingId.value = id
  try {
    await handleReport(id, { status })
    ElMessage.success('处理完成')
    await loadAdminReports()
  } finally {
    adminHandleLoadingId.value = null
  }
}

onMounted(async () => {
  if (!userStore.profile) {
    await userStore.refreshProfile()
  }
  await Promise.all([loadPets(), loadTopics(), loadFeed(), loadRecommendations(), loadHotPets()])
})
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  padding: 20px;
  background:
    radial-gradient(circle at 0% 0%, rgba(255, 229, 198, 0.55) 0%, rgba(255, 229, 198, 0) 36%),
    radial-gradient(circle at 100% 0%, rgba(192, 224, 255, 0.45) 0%, rgba(192, 224, 255, 0) 32%),
    #f8fbff;
}

.community-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.community-header h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.community-header p {
  margin: 0;
  color: #677083;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.feed-filter,
.feed-item,
.side-card {
  border-radius: 14px;
}

.feed-filter {
  margin-bottom: 12px;
}

.filter-row {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1fr 1fr auto auto;
  gap: 8px;
}

.feed-list {
  display: grid;
  gap: 12px;
}

.feed-item {
  border: 1px solid #edf1f8;
}

.feed-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.feed-author-wrap {
  display: flex;
  gap: 10px;
}

.pet-mini-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.feed-top h3 {
  margin: 0 0 6px;
}

.feed-meta {
  color: #6f7889;
  font-size: 13px;
}

.feed-tags {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.feed-content {
  margin: 10px 0;
  white-space: pre-wrap;
}

.feed-image {
  width: min(420px, 100%);
  border-radius: 10px;
  border: 1px solid #edf1f8;
  cursor: zoom-in;
}

.feed-image :deep(img) {
  border-radius: 10px;
}

.feed-video-wrap {
  margin-top: 10px;
  position: relative;
  width: min(460px, 100%);
}

.feed-video {
  width: 100%;
  max-height: 320px;
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

.feed-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.switch-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.upload-row {
  width: 100%;
  display: flex;
  gap: 8px;
}

.upload-row :deep(.el-input) {
  flex: 1;
}

.topic-select-row {
  width: 100%;
  display: flex;
  gap: 8px;
}

.topic-select-row :deep(.el-select) {
  flex: 1;
}

.side-card {
  margin-bottom: 12px;
}

.user-search-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.user-search-row :deep(.el-input) {
  flex: 1;
}

.user-search-list {
  display: grid;
  gap: 8px;
}

.user-search-item {
  border: 1px solid #edf1f8;
  border-radius: 10px;
  background: #fff;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
}

.user-search-item strong {
  color: #2f3543;
}

.user-search-item p {
  margin-top: 6px;
  color: #6d7587;
  font-size: 12px;
}

.user-search-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e4e9f5;
}

.recommend-list {
  display: grid;
  gap: 8px;
}

.recommend-item {
  border: 1px solid #edf1f8;
  background: #fff;
  border-radius: 10px;
  padding: 10px;
  text-align: left;
  display: grid;
  gap: 4px;
  cursor: pointer;
}

.recommend-item strong {
  color: #2f3543;
}

.recommend-item span {
  color: #6d7587;
  font-size: 12px;
}

.recommend-score {
  color: #3576da;
}

.hot-rank-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.hot-rank-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #edf1f8;
  border-radius: 10px;
  padding: 8px;
  cursor: pointer;
}

.hot-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hot-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #ffefe0;
  color: #df7c34;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.hot-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.hot-left p {
  margin: 0;
  color: #6d7587;
  font-size: 12px;
}

.hot-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  color: #2d3442;
}

.hot-right small {
  color: #6d7587;
}

.comment-list {
  display: grid;
  gap: 10px;
  max-height: 280px;
  overflow: auto;
}

.comment-item {
  border: 1px solid #ebeef4;
  border-radius: 8px;
  padding: 8px;
}

.comment-item p {
  margin: 6px 0 0;
  white-space: pre-wrap;
}

.message-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 12px;
  min-height: 420px;
}

.conversation-pane {
  border: 1px solid #edf1f8;
  border-radius: 10px;
  padding: 8px;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.conversation-list {
  display: grid;
  gap: 8px;
}

.conversation-item {
  border: 1px solid #e8edf7;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  padding: 8px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  cursor: pointer;
}

.conversation-item.active {
  border-color: #5b8fe4;
  background: #f1f6ff;
}

.conversation-item p {
  margin-top: 6px;
  color: #6f7889;
  font-size: 12px;
}

.unread-dot {
  display: inline-flex;
  min-width: 20px;
  height: 20px;
  border-radius: 999px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

.chat-pane {
  border: 1px solid #edf1f8;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-title {
  margin: 0;
}

.chat-message-list {
  flex: 1;
  border: 1px solid #edf1f8;
  border-radius: 8px;
  padding: 8px;
  overflow: auto;
  max-height: 320px;
  display: grid;
  gap: 8px;
}

.chat-empty {
  color: #7a8393;
  font-size: 13px;
}

.chat-message-item {
  display: flex;
}

.chat-message-item p {
  margin: 0;
  max-width: 70%;
  background: #f3f6fb;
  border-radius: 8px;
  padding: 8px;
  white-space: pre-wrap;
}

.chat-message-item.mine {
  justify-content: flex-end;
}

.chat-message-item.mine p {
  background: #e9f3ff;
}

.chat-send-row {
  display: flex;
  gap: 8px;
}

.chat-send-row :deep(.el-textarea) {
  flex: 1;
}

.admin-report-toolbar {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
}

@media (max-width: 992px) {
  .community-page {
    padding: 14px;
  }

  .community-header {
    flex-direction: column;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .topic-select-row,
  .chat-send-row,
  .user-search-row {
    flex-direction: column;
  }

  .message-layout {
    grid-template-columns: 1fr;
  }
}
</style>
