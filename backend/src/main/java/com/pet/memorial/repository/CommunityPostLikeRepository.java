package com.pet.memorial.repository;

import com.pet.memorial.entity.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    Optional<CommunityPostLike> findByPostIdAndUsername(Long postId, String username);

    long countByPostId(Long postId);

    boolean existsByPostIdAndUsername(Long postId, String username);
}
