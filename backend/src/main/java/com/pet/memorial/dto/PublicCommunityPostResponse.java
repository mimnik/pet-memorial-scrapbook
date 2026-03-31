package com.pet.memorial.dto;

import java.time.LocalDateTime;

public class PublicCommunityPostResponse {

    private Long id;
    private Long petId;
    private String petName;
    private String topicName;
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
    private LocalDateTime createdAt;

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

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
