import request from '@/utils/request'
import type {
  PetArchiveRecord,
  PetArchiveRecordPayload,
  PetArchiveReminderSnoozePayload,
} from '@/types/archive'

export const listPetArchivesByPet = (petId: number) =>
  request.get<unknown, { data: PetArchiveRecord[] }>(`/pets/${petId}/archives`)

export const createPetArchive = (petId: number, payload: PetArchiveRecordPayload) =>
  request.post<unknown, { data: PetArchiveRecord }>(`/pets/${petId}/archives`, payload)

export const updatePetArchive = (id: number, payload: PetArchiveRecordPayload) =>
  request.put<unknown, { data: PetArchiveRecord }>(`/pet-archives/${id}`, payload)

export const deletePetArchive = (id: number) => request.delete(`/pet-archives/${id}`)

export const listDueArchiveReminders = () =>
  request.get<unknown, { data: PetArchiveRecord[] }>('/pet-archives/reminders/due')

export const completeArchiveReminder = (id: number) =>
  request.post<unknown, { data: PetArchiveRecord }>(`/pet-archives/${id}/reminder/complete`)

export const snoozeArchiveReminder = (id: number, payload: PetArchiveReminderSnoozePayload) =>
  request.post<unknown, { data: PetArchiveRecord }>(`/pet-archives/${id}/reminder/snooze`, payload)
