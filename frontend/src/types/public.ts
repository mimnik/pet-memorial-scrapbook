import type { MemoryEntry } from '@/types/memory'
import type { Pet } from '@/types/pet'

export interface PublicPetView {
  pet: Pet
  memories: MemoryEntry[]
}

export interface UserPublicHome {
  ownerUsername: string
  publicPets: Pet[]
  activePet: Pet | null
  memories: MemoryEntry[]
}
