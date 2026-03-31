package com.pet.memorial.dto;

import java.time.LocalDateTime;

public class MessageConversationResponse {

    private String peerUsername;
    private String peerDisplayName;
    private String peerAvatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;

    public String getPeerUsername() {
        return peerUsername;
    }

    public void setPeerUsername(String peerUsername) {
        this.peerUsername = peerUsername;
    }

    public String getPeerDisplayName() {
        return peerDisplayName;
    }

    public void setPeerDisplayName(String peerDisplayName) {
        this.peerDisplayName = peerDisplayName;
    }

    public String getPeerAvatarUrl() {
        return peerAvatarUrl;
    }

    public void setPeerAvatarUrl(String peerAvatarUrl) {
        this.peerAvatarUrl = peerAvatarUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
