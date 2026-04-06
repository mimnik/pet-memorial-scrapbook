import request from '@/utils/request'
import type {
  AccountAppeal,
  AccountAppealHandlePayload,
  AdminCommunityComment,
  AdminCommunityPost,
  AdminDashboardOverview,
  AdminModerationPayload,
  AdminUser,
  AdminUserStatusPayload,
  AdminUserWarnPayload,
} from '@/types/admin'

export const listAdminUsers = (keyword?: string) =>
  request.get<unknown, { data: AdminUser[] }>('/admin/users', { params: { keyword } })

export const updateAdminUserStatus = (id: number, payload: AdminUserStatusPayload) =>
  request.put<unknown, { data: AdminUser }>(`/admin/users/${id}/status`, payload)

export const warnAdminUser = (id: number, payload: AdminUserWarnPayload) =>
  request.post<unknown, { data: AdminUser }>(`/admin/users/${id}/warn`, payload)

export const listAdminAppeals = (status?: string) =>
  request.get<unknown, { data: AccountAppeal[] }>('/admin/account-appeals', { params: { status } })

export const handleAdminAppeal = (id: number, payload: AccountAppealHandlePayload) =>
  request.put<unknown, { data: AccountAppeal }>(`/admin/account-appeals/${id}/handle`, payload)

export const addUser = (payload: { username?: string, role: string }) =>
  request.post<unknown, { data: AdminUser }>('/admin/users', payload)

export const listAdminCommunityPosts = (keyword?: string) =>
  request.get<unknown, { data: AdminCommunityPost[] }>('/admin/community/posts', { params: { keyword } })

export const listAdminCommunityComments = (params?: { postId?: number; keyword?: string }) =>
  request.get<unknown, { data: AdminCommunityComment[] }>('/admin/community/comments', { params })

export const moderateAdminPost = (id: number, payload: AdminModerationPayload) =>
  request.put<unknown, { data: AdminCommunityPost }>(`/admin/community/posts/${id}/moderate`, payload)

export const moderateAdminComment = (id: number, payload: AdminModerationPayload) =>
  request.put<unknown, { data: AdminCommunityComment }>(`/admin/community/comments/${id}/moderate`, payload)

export const getAdminDashboardOverview = () =>
  request.get<unknown, { data: AdminDashboardOverview }>('/admin/dashboard/overview')
