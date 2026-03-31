package com.pet.memorial.dto;

public class PublicUserSearchItemResponse {

    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Integer publicPetCount;
    private Integer recentPostCount;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public Integer getPublicPetCount() {
        return publicPetCount;
    }

    public void setPublicPetCount(Integer publicPetCount) {
        this.publicPetCount = publicPetCount;
    }

    public Integer getRecentPostCount() {
        return recentPostCount;
    }

    public void setRecentPostCount(Integer recentPostCount) {
        this.recentPostCount = recentPostCount;
    }
}
