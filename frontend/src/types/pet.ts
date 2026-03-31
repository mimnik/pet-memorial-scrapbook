export interface Pet {
  id: number
  name: string
  species?: string
  breed?: string
  gender?: string
  birthDate?: string
  memorialDate?: string
  avatarUrl?: string
  description?: string
  isPublic: boolean
  shareToken: string
  createdAt: string
  updatedAt: string
}

export interface PetPayload {
  name: string
  species?: string
  breed?: string
  gender?: string
  birthDate?: string
  memorialDate?: string
  avatarUrl?: string
  description?: string
  isPublic?: boolean
}
