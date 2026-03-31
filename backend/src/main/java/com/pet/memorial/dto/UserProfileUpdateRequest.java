package com.pet.memorial.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(max = 100, message = "昵称长度不能超过100")
    private String displayName;

    @Size(max = 500, message = "头像地址长度不能超过500")
    private String avatarUrl;

    @Size(max = 1000, message = "个人简介长度不能超过1000")
    private String bio;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
