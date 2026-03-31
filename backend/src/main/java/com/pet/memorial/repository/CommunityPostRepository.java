package com.pet.memorial.repository;

import com.pet.memorial.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("""
        select p from CommunityPost p
        where (:mode is null or p.narrativeMode = :mode)
          and (:mood is null or p.moodTag = :mood)
          and (:topicId is null or p.topic.id = :topicId)
          and (
            :keyword is null
            or lower(p.title) like lower(concat('%', :keyword, '%'))
            or lower(p.content) like lower(concat('%', :keyword, '%'))
            or lower(p.pet.name) like lower(concat('%', :keyword, '%'))
            or lower(p.authorUsername) like lower(concat('%', :keyword, '%'))
          )
        order by p.createdAt desc, p.id desc
        """)
    List<CommunityPost> findFeed(
        @Param("mode") String mode,
        @Param("mood") String mood,
        @Param("keyword") String keyword,
        @Param("topicId") Long topicId
    );

    List<CommunityPost> findByAuthorUsernameOrderByCreatedAtDescIdDesc(String authorUsername);

    List<CommunityPost> findByAuthorUsernameInOrderByCreatedAtDescIdDesc(List<String> authorUsernames);

    List<CommunityPost> findAllByOrderByCreatedAtDescIdDesc();

    int countByAuthorUsername(String authorUsername);
}
