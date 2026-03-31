package com.pet.memorial.dto;

public class PetHotRankResponse {

    private Long petId;
    private String petShareToken;
    private String petName;
    private String petAvatarUrl;
    private String ownerUsername;
    private Integer postCount;
    private Integer totalLikes;
    private Integer totalComments;
    private Double heatScore;

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetShareToken() {
        return petShareToken;
    }

    public void setPetShareToken(String petShareToken) {
        this.petShareToken = petShareToken;
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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Integer totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Integer getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(Integer totalComments) {
        this.totalComments = totalComments;
    }

    public Double getHeatScore() {
        return heatScore;
    }

    public void setHeatScore(Double heatScore) {
        this.heatScore = heatScore;
    }
}
