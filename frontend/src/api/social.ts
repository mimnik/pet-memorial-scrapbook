import request from '@/utils/request'
import type { FollowStatus, FollowSummary, FollowUser } from '@/types/social'

export const followUser = (username: string) =>
  request.post<unknown, { data: FollowStatus }>(`/social/follow/${encodeURIComponent(username)}`)

export const unfollowUser = (username: string) =>
  request.delete<unknown, { data: FollowStatus }>(`/social/follow/${encodeURIComponent(username)}`)

export const listFollowingUsers = () => request.get<unknown, { data: FollowUser[] }>('/social/following')

export const listFollowerUsers = () => request.get<unknown, { data: FollowUser[] }>('/social/followers')

export const getFollowSummary = () => request.get<unknown, { data: FollowSummary }>('/social/summary')
