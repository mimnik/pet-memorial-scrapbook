package com.pet.memorial.dto;

public class AdminDashboardOverviewResponse {

    private long totalUsers;
    private long frozenUsers;
    private long restrictedUsers;
    private long totalPosts;
    private long totalComments;
    private long totalMemories;
    private long totalTopics;
    private long multimediaFileCount;
    private long multimediaStorageBytes;
    private boolean authApiHealthy;
    private boolean communityApiHealthy;
    private boolean uploadApiHealthy;
    private String uploadDir;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getFrozenUsers() {
        return frozenUsers;
    }

    public void setFrozenUsers(long frozenUsers) {
        this.frozenUsers = frozenUsers;
    }

    public long getRestrictedUsers() {
        return restrictedUsers;
    }

    public void setRestrictedUsers(long restrictedUsers) {
        this.restrictedUsers = restrictedUsers;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getTotalMemories() {
        return totalMemories;
    }

    public void setTotalMemories(long totalMemories) {
        this.totalMemories = totalMemories;
    }

    public long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public long getMultimediaFileCount() {
        return multimediaFileCount;
    }

    public void setMultimediaFileCount(long multimediaFileCount) {
        this.multimediaFileCount = multimediaFileCount;
    }

    public long getMultimediaStorageBytes() {
        return multimediaStorageBytes;
    }

    public void setMultimediaStorageBytes(long multimediaStorageBytes) {
        this.multimediaStorageBytes = multimediaStorageBytes;
    }

    public boolean isAuthApiHealthy() {
        return authApiHealthy;
    }

    public void setAuthApiHealthy(boolean authApiHealthy) {
        this.authApiHealthy = authApiHealthy;
    }

    public boolean isCommunityApiHealthy() {
        return communityApiHealthy;
    }

    public void setCommunityApiHealthy(boolean communityApiHealthy) {
        this.communityApiHealthy = communityApiHealthy;
    }

    public boolean isUploadApiHealthy() {
        return uploadApiHealthy;
    }

    public void setUploadApiHealthy(boolean uploadApiHealthy) {
        this.uploadApiHealthy = uploadApiHealthy;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
