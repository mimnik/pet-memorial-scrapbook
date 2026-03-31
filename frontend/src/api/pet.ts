import request from '@/utils/request'
import type { Pet, PetPayload } from '@/types/pet'

export const listPets = () => request.get<unknown, { data: Pet[] }>('/pets')

export const getPetById = (id: number) => request.get<unknown, { data: Pet }>(`/pets/${id}`)

export const createPet = (payload: PetPayload) =>
  request.post<unknown, { data: Pet }>('/pets', payload)

export const updatePet = (id: number, payload: PetPayload) =>
  request.put<unknown, { data: Pet }>(`/pets/${id}`, payload)

export const deletePet = (id: number) => request.delete(`/pets/${id}`)
