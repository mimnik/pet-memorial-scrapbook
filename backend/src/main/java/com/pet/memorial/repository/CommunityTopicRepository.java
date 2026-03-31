package com.pet.memorial.repository;

import com.pet.memorial.entity.CommunityTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityTopicRepository extends JpaRepository<CommunityTopic, Long> {

    Optional<CommunityTopic> findByNameIgnoreCase(String name);

    List<CommunityTopic> findAllByOrderByCreatedAtDescIdDesc();
}
