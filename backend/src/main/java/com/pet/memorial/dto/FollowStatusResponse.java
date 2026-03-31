package com.pet.memorial.dto;

public class FollowStatusResponse {

    private String targetUsername;
    private Boolean following;
    private Long followingCount;
    private Long followerCount;

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Long followerCount) {
        this.followerCount = followerCount;
    }
}
