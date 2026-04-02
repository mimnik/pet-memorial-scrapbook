export interface AdminUser {
  id: number
  username: string
  email: string
  displayName?: string
  role: string
  accountFrozen: boolean
  postingRestricted: boolean
  warningCount: number
  adminNote?: string
  createdAt: string
}

export interface AdminUserStatusPayload {
  accountFrozen?: boolean
  postingRestricted?: boolean
  adminNote?: string
}

export interface AdminUserWarnPayload {
  note?: string
}

export interface AccountAppeal {
  id: number
  username: string
  appealType: string
  details: string
  status: 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'REJECTED' | string
  handledByUsername?: string
  handledAt?: string
  handleNote?: string
  createdAt: string
}

export interface AccountAppealHandlePayload {
  status: 'PROCESSING' | 'RESOLVED' | 'REJECTED'
  handleNote?: string
}

export interface AdminModerationPayload {
  action: 'DELETE' | 'SHIELD'
  reason?: string
}

export interface AdminCommunityPost {
  id: number
  authorUsername: string
  petName: string
  title: string
  content: string
  moodTag: string
  narrativeMode: string
  likeCount: number
  commentCount: number
  hiddenByAdmin: boolean
  createdAt: string
}

export interface AdminCommunityComment {
  id: number
  postId: number
  postTitle: string
  authorUsername: string
  content: string
  relayReply: boolean
  hiddenByAdmin: boolean
  createdAt: string
}

export interface AdminDashboardOverview {
  totalUsers: number
  frozenUsers: number
  restrictedUsers: number
  totalPosts: number
  totalComments: number
  totalMemories: number
  totalTopics: number
  multimediaFileCount: number
  multimediaStorageBytes: number
  authApiHealthy: boolean
  communityApiHealthy: boolean
  uploadApiHealthy: boolean
  uploadDir: string
}
