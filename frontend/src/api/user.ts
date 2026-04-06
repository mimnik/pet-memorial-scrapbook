import request from '@/utils/request'
import type { UserProfile, UserProfileUpdatePayload } from '@/types/user'

export const getMyProfile = () => request.get<unknown, { data: UserProfile }>('/users/me')

export const updateMyProfile = (payload: UserProfileUpdatePayload) =>
  request.put<unknown, { data: UserProfile }>('/users/me', payload)

export const changePassword = (data: any) =>
  request.put<void, any>('/users/me/password', data)
