export interface MemoryEntry {
  id: number
  pet: {
    id: number
    name: string
  }
  title: string
  content: string
  eventDate?: string
  location?: string
  imageUrl?: string
  videoUrl?: string
  videoCoverUrl?: string
  videoDurationSeconds?: number
  createdAt: string
  updatedAt: string
}

export interface MemoryEntryPayload {
  title: string
  content: string
  eventDate?: string
  location?: string
  imageUrl?: string
  videoUrl?: string
  videoCoverUrl?: string
  videoDurationSeconds?: number
}
