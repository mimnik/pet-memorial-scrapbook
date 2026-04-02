package com.pet.memorial.dto;

import java.time.LocalDateTime;

public class AdminCommunityCommentResponse {

    private Long id;
    private Long postId;
    private String postTitle;
    private String authorUsername;
    private String content;
    private Boolean relayReply;
    private Boolean hiddenByAdmin;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getRelayReply() {
        return relayReply;
    }

    public void setRelayReply(Boolean relayReply) {
        this.relayReply = relayReply;
    }

    public Boolean getHiddenByAdmin() {
        return hiddenByAdmin;
    }

    public void setHiddenByAdmin(Boolean hiddenByAdmin) {
        this.hiddenByAdmin = hiddenByAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
