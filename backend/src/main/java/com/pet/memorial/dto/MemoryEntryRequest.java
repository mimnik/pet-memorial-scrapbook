package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MemoryEntryRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000")
    private String content;

    private LocalDate eventDate;

    @Size(max = 200, message = "地点长度不能超过200")
    private String location;

    @Size(max = 500, message = "图片地址长度不能超过500")
    private String imageUrl;

    @Size(max = 500, message = "视频地址长度不能超过500")
    private String videoUrl;

    @Size(max = 500, message = "视频预览图地址长度不能超过500")
    private String videoCoverUrl;

    private Integer videoDurationSeconds;

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

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
}
