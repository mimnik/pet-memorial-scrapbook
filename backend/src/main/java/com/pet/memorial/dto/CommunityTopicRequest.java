package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommunityTopicRequest {

    @NotBlank(message = "话题名称不能为空")
    @Size(max = 50, message = "话题名称长度不能超过50")
    private String name;

    @Size(max = 500, message = "话题描述长度不能超过500")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
