package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommunityPostRequest {

    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    private Long topicId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000")
    private String content;

    @Size(max = 500, message = "图片地址长度不能超过500")
    private String imageUrl;

    @Size(max = 500, message = "视频地址长度不能超过500")
    private String videoUrl;

    @Size(max = 500, message = "视频预览图地址长度不能超过500")
    private String videoCoverUrl;

    private Integer videoDurationSeconds;

    @Size(max = 20, message = "情绪标签长度不能超过20")
    private String moodTag;

    @Size(max = 20, message = "叙事模式长度不能超过20")
    private String narrativeMode;

    private Boolean petVoice;

    private Boolean relayEnabled;

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
}
