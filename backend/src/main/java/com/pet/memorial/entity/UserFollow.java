package com.pet.memorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_follows",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_follows_pair", columnNames = {"follower_username", "following_username"}),
    indexes = {
        @Index(name = "idx_user_follows_follower", columnList = "follower_username"),
        @Index(name = "idx_user_follows_following", columnList = "following_username")
    }
)
public class UserFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String followerUsername;

    @Column(nullable = false, length = 100)
    private String followingUsername;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFollowerUsername() {
        return followerUsername;
    }

    public void setFollowerUsername(String followerUsername) {
        this.followerUsername = followerUsername;
    }

    public String getFollowingUsername() {
        return followingUsername;
    }

    public void setFollowingUsername(String followingUsername) {
        this.followingUsername = followingUsername;
    }
}
