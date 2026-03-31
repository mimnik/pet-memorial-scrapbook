export type ReportTargetType = 'POST' | 'COMMENT'
export type ReportStatus = 'PENDING' | 'RESOLVED' | 'REJECTED'

export interface ReportItem {
  id: number
  reporterUsername: string
  targetType: ReportTargetType
  targetId: number
  reason: string
  details?: string
  status: ReportStatus
  handledByUsername?: string
  handledAt?: string
  handleNote?: string
  createdAt: string
}

export interface ReportCreatePayload {
  targetType: ReportTargetType
  targetId: number
  reason: string
  details?: string
}

export interface ReportHandlePayload {
  status: Exclude<ReportStatus, 'PENDING'>
  handleNote?: string
}
