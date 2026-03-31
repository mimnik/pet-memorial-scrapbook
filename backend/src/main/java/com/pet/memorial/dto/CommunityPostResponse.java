package com.pet.memorial.dto;

import java.time.LocalDateTime;

public class CommunityPostResponse {

    private Long id;
    private Long petId;
    private Long topicId;
    private String topicName;
    private String petName;
    private String petAvatarUrl;
    private String authorUsername;
    private String title;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String videoCoverUrl;
    private Integer videoDurationSeconds;
    private String moodTag;
    private String narrativeMode;
    private Boolean petVoice;
    private Boolean relayEnabled;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean likedByMe;
    private Boolean authorFollowedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double recommendationScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetAvatarUrl() {
        return petAvatarUrl;
    }

    public void setPetAvatarUrl(String petAvatarUrl) {
        this.petAvatarUrl = petAvatarUrl;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoCoverUrl() {
        return videoCoverUrl;
    }

    public void setVideoCoverUrl(String videoCoverUrl) {
        this.videoCoverUrl = videoCoverUrl;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
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

    public Boolean getPetVoice() {
        return petVoice;
    }

    public void setPetVoice(Boolean petVoice) {
        this.petVoice = petVoice;
    }

    public Boolean getRelayEnabled() {
        return relayEnabled;
    }

    public void setRelayEnabled(Boolean relayEnabled) {
        this.relayEnabled = relayEnabled;
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

    public Boolean getLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(Boolean likedByMe) {
        this.likedByMe = likedByMe;
    }

    public Boolean getAuthorFollowedByMe() {
        return authorFollowedByMe;
    }

    public void setAuthorFollowedByMe(Boolean authorFollowedByMe) {
        this.authorFollowedByMe = authorFollowedByMe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(Double recommendationScore) {
        this.recommendationScore = recommendationScore;
    }
}
