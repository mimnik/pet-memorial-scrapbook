import request from '@/utils/request'
import type { ReportCreatePayload, ReportHandlePayload, ReportItem, ReportStatus } from '@/types/report'

export const createReport = (payload: ReportCreatePayload) =>
  request.post<unknown, { data: ReportItem }>('/reports', payload)

export const listMyReports = () => request.get<unknown, { data: ReportItem[] }>('/reports/mine')

export const listAdminReports = (status?: ReportStatus) =>
  request.get<unknown, { data: ReportItem[] }>('/reports/admin', { params: { status } })

export const handleReport = (id: number, payload: ReportHandlePayload) =>
  request.put<unknown, { data: ReportItem }>(`/reports/admin/${id}/handle`, payload)
