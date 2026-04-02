package com.pet.memorial.repository;

import com.pet.memorial.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);

    List<CommunityComment> findAllByOrderByCreatedAtDescIdDesc();

    long countByPostId(Long postId);

    long countByPostIdAndHiddenByAdminFalse(Long postId);
}
