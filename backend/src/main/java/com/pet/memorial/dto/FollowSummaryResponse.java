package com.pet.memorial.dto;

public class FollowSummaryResponse {

    private Long followingCount;
    private Long followerCount;

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
