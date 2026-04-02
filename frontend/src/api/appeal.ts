import request from '@/utils/request'
import type { FrozenAccountAppealPayload } from '@/types/appeal'

export const createFrozenAccountAppeal = (payload: FrozenAccountAppealPayload) =>
  request.post<unknown, { data: unknown }>('/account-appeals/public', payload)
