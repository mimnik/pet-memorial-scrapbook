export interface FollowStatus {
  targetUsername: string
  following: boolean
  followingCount: number
  followerCount: number
}

export interface FollowUser {
  username: string
  displayName?: string
  avatarUrl?: string
  bio?: string
  followedAt: string
}

export interface FollowSummary {
  followingCount: number
  followerCount: number
}
