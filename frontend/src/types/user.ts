export interface UserProfile {
  username: string
  email: string
  role: string
  displayName?: string
  avatarUrl?: string
  bio?: string
  followingCount: number
  followerCount: number
}

export interface UserProfileUpdatePayload {
  displayName?: string
  avatarUrl?: string
  bio?: string
}
