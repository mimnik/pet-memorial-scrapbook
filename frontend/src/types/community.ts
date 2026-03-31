export type CommunityMoodTag = 'SUNNY' | 'CLOUDY' | 'RAINY' | 'STORMY' | 'RAINBOW'
export type CommunityNarrativeMode = 'DAILY' | 'MEMORIAL'

export interface CommunityPost {
  id: number
  petId: number
  topicId?: number
  topicName?: string
  petName: string
  petAvatarUrl?: string
  authorUsername: string
  title: string
  content: string
  imageUrl?: string
  videoUrl?: string
  videoCoverUrl?: string
  videoDurationSeconds?: number
  moodTag: CommunityMoodTag
  narrativeMode: CommunityNarrativeMode
  petVoice: boolean
  relayEnabled: boolean
  likeCount: number
  commentCount: number
  likedByMe: boolean
  authorFollowedByMe?: boolean
  createdAt: string
  updatedAt: string
  recommendationScore?: number
}

export interface CommunityPostPayload {
  petId: number
  topicId?: number
  title: string
  content: string
  imageUrl?: string
  videoUrl?: string
  videoCoverUrl?: string
  videoDurationSeconds?: number
  moodTag?: CommunityMoodTag
  narrativeMode?: CommunityNarrativeMode
  petVoice?: boolean
  relayEnabled?: boolean
}

export interface CommunityFeedParams {
  mode?: string
  mood?: string
  keyword?: string
  topicId?: number
}

export interface CommunityTopic {
  id: number
  name: string
  description?: string
  createdByUsername: string
  createdAt: string
}

export interface CommunityTopicPayload {
  name: string
  description?: string
}

export interface PetHotRank {
  petId: number
  petName: string
  petAvatarUrl?: string
  ownerUsername: string
  postCount: number
  totalLikes: number
  totalComments: number
  heatScore: number
}

export interface CommunityComment {
  id: number
  postId: number
  authorUsername: string
  content: string
  relayReply: boolean
  createdAt: string
  updatedAt: string
}

export interface CommunityCommentPayload {
  content: string
  relayReply?: boolean
}
