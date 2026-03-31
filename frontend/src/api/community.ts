import request from '@/utils/request'
import type {
  CommunityComment,
  CommunityCommentPayload,
  CommunityFeedParams,
  CommunityPost,
  CommunityPostPayload,
  CommunityTopic,
  CommunityTopicPayload,
  PetHotRank,
} from '@/types/community'

export const listCommunityFeed = (params?: CommunityFeedParams) =>
  request.get<unknown, { data: CommunityPost[] }>('/community/feed', { params })

export const listFollowingCommunityFeed = () =>
  request.get<unknown, { data: CommunityPost[] }>('/community/following-feed')

export const listCommunityRecommendations = (limit = 6) =>
  request.get<unknown, { data: CommunityPost[] }>('/community/recommendations', {
    params: { limit },
  })

export const listCommunityHotPets = (limit = 10) =>
  request.get<unknown, { data: PetHotRank[] }>('/community/hot-pets', {
    params: { limit },
  })

export const listMyCommunityPosts = () => request.get<unknown, { data: CommunityPost[] }>('/community/mine')

export const createCommunityPost = (payload: CommunityPostPayload) =>
  request.post<unknown, { data: CommunityPost }>('/community/posts', payload)

export const listCommunityTopics = () =>
  request.get<unknown, { data: CommunityTopic[] }>('/community/topics')

export const createCommunityTopic = (payload: CommunityTopicPayload) =>
  request.post<unknown, { data: CommunityTopic }>('/community/topics', payload)

export const toggleCommunityLike = (postId: number) =>
  request.post<unknown, { data: CommunityPost }>(`/community/posts/${postId}/like`)

export const listCommunityComments = (postId: number) =>
  request.get<unknown, { data: CommunityComment[] }>(`/community/posts/${postId}/comments`)

export const createCommunityComment = (postId: number, payload: CommunityCommentPayload) =>
  request.post<unknown, { data: CommunityComment }>(`/community/posts/${postId}/comments`, payload)
