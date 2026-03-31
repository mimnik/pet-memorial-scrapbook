import request from '@/utils/request'
import type { MessageConversation, MessageItem, MessagePayload, MessageSummary } from '@/types/message'

export const listMessageConversations = () =>
  request.get<unknown, { data: MessageConversation[] }>('/messages/conversations')

export const listMessagesWithUser = (username: string) =>
  request.get<unknown, { data: MessageItem[] }>(`/messages/with/${encodeURIComponent(username)}`)

export const sendMessageToUser = (username: string, payload: MessagePayload) =>
  request.post<unknown, { data: MessageItem }>(`/messages/with/${encodeURIComponent(username)}`, payload)

export const markMessagesReadWithUser = (username: string) =>
  request.put(`/messages/with/${encodeURIComponent(username)}/read`)

export const getMessageSummary = () => request.get<unknown, { data: MessageSummary }>('/messages/summary')
