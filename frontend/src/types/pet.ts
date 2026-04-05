export interface Pet {
  id: number
  name: string
  species?: string
  breed?: string
  gender?: string
  birthDate?: string
  memorialDate?: string
  age?: number
  weight?: string
  maritalStatus?: string
  skills?: string
  dietaryHabits?: string
  physicalCondition?: string
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
  weight?: string
  maritalStatus?: string
  skills?: string
  dietaryHabits?: string
  physicalCondition?: string
  avatarUrl?: string
  description?: string
  isPublic?: boolean
}
