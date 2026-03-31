import request from '@/utils/request'
import type { PublicPetView, PublicUserSearchItem, UserPublicHome } from '@/types/public'

export const getPublicPetByToken = (shareToken: string) =>
  request.get<unknown, { data: PublicPetView }>(`/public/pets/${shareToken}`)

export const getUserPublicHome = (ownerUsername: string, pet?: string) =>
  request.get<unknown, { data: UserPublicHome }>(`/public/users/${ownerUsername}/home`, {
    params: {
      pet,
    },
  })

export const searchPublicUsers = (keyword: string, limit = 12) =>
  request.get<unknown, { data: PublicUserSearchItem[] }>('/public/users/search', {
    params: {
      keyword,
      limit,
    },
  })
