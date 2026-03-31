import request from '@/utils/request'
import type { MemoryEntry, MemoryEntryPayload } from '@/types/memory'

export const listMemoriesByPet = (petId: number) =>
  request.get<unknown, { data: MemoryEntry[] }>(`/pets/${petId}/memories`)

export const createMemory = (petId: number, payload: MemoryEntryPayload) =>
  request.post<unknown, { data: MemoryEntry }>(`/pets/${petId}/memories`, payload)

export const updateMemory = (id: number, payload: MemoryEntryPayload) =>
  request.put<unknown, { data: MemoryEntry }>(`/memories/${id}`, payload)

export const deleteMemory = (id: number) => request.delete(`/memories/${id}`)
