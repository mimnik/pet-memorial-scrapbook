package com.pet.memorial.repository;

import com.pet.memorial.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerUsernameAndFollowingUsername(String followerUsername, String followingUsername);

    boolean existsByFollowerUsernameAndFollowingUsername(String followerUsername, String followingUsername);

    long countByFollowerUsername(String followerUsername);

    long countByFollowingUsername(String followingUsername);

    List<UserFollow> findByFollowerUsernameOrderByCreatedAtDesc(String followerUsername);

    List<UserFollow> findByFollowingUsernameOrderByCreatedAtDesc(String followingUsername);

    @Query("select f.followingUsername from UserFollow f where f.followerUsername = :followerUsername")
    List<String> findFollowingUsernamesByFollowerUsername(@Param("followerUsername") String followerUsername);
}
