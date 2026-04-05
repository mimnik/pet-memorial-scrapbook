export type PetArchiveType = 'VACCINE' | 'CHECKUP' | 'DEWORMING' | 'MEDICATION' | 'OTHER'
export type ReminderStatus = 'PENDING' | 'COMPLETED'

export interface PetArchiveRecord {
  id: number
  pet: {
    id: number
    name: string
  }
  archiveType: PetArchiveType | string
  title: string
  metricValue?: string
  unit?: string
  eventDate?: string
  note?: string
  reminderEnabled: boolean
  reminderAt?: string
  reminderStatus: ReminderStatus | string
  reminderCompletedAt?: string
  createdAt: string
  updatedAt: string
}

export interface PetArchiveRecordPayload {
  archiveType: PetArchiveType | string
  title: string
  metricValue?: string
  unit?: string
  eventDate?: string
  note?: string
  reminderEnabled?: boolean
  reminderAt?: string
}

export interface PetArchiveReminderSnoozePayload {
  delayHours: number
}
