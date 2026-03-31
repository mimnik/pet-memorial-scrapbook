export interface MessageItem {
  id: number
  senderUsername: string
  receiverUsername: string
  content: string
  readByReceiver: boolean
  fromMe: boolean
  createdAt: string
}

export interface MessageConversation {
  peerUsername: string
  peerDisplayName?: string
  peerAvatarUrl?: string
  lastMessage: string
  lastMessageAt: string
  unreadCount: number
}

export interface MessageSummary {
  unreadCount: number
}

export interface MessagePayload {
  content: string
}
