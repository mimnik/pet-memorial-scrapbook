import type { MemoryEntry } from '@/types/memory'
import type { Pet } from '@/types/pet'

export interface PublicCommunityPost {
  id: number
  petId: number
  petName: string
  topicName?: string
  title: string
  content: string
  imageUrl?: string
  videoUrl?: string
  videoCoverUrl?: string
  videoDurationSeconds?: number
  moodTag: string
  narrativeMode: string
  petVoice: boolean
  relayEnabled: boolean
  likeCount: number
  commentCount: number
  createdAt: string
}

export interface PublicUserSearchItem {
  username: string
  displayName?: string
  avatarUrl?: string
  bio?: string
  publicPetCount: number
  recentPostCount: number
}

export interface PublicPetView {
  pet: Pet
  memories: MemoryEntry[]
}

export interface UserPublicHome {
  ownerUsername: string
  publicPets: Pet[]
  activePet: Pet | null
  memories: MemoryEntry[]
  communityPosts: PublicCommunityPost[]
}
