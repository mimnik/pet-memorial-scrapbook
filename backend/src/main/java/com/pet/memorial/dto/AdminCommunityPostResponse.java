package com.pet.memorial.dto;

import java.time.LocalDateTime;

public class AdminCommunityPostResponse {

    private Long id;
    private String authorUsername;
    private String petName;
    private String title;
    private String content;
    private String moodTag;
    private String narrativeMode;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean hiddenByAdmin;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMoodTag() {
        return moodTag;
    }

    public void setMoodTag(String moodTag) {
        this.moodTag = moodTag;
    }

    public String getNarrativeMode() {
        return narrativeMode;
    }

    public void setNarrativeMode(String narrativeMode) {
        this.narrativeMode = narrativeMode;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
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
