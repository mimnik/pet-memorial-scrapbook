package com.pet.memorial.service;

import com.pet.memorial.dto.CommunityCommentRequest;
import com.pet.memorial.dto.CommunityCommentResponse;
import com.pet.memorial.dto.CommunityPostRequest;
import com.pet.memorial.dto.CommunityPostResponse;
import com.pet.memorial.dto.CommunityTopicRequest;
import com.pet.memorial.dto.CommunityTopicResponse;
import com.pet.memorial.dto.PetHotRankResponse;

import java.util.List;

public interface CommunityOperations {

    List<CommunityPostResponse> listFeed(String narrativeMode, String moodTag, String keyword, Long topicId);

    List<CommunityPostResponse> listFollowingFeed();

    List<CommunityPostResponse> listRecommendations(int limit);

    List<PetHotRankResponse> listHotPets(int limit);

    List<CommunityPostResponse> listMine();

    CommunityPostResponse createPost(CommunityPostRequest request);

    CommunityPostResponse toggleLike(Long postId);

    List<CommunityCommentResponse> listComments(Long postId);

    CommunityCommentResponse createComment(Long postId, CommunityCommentRequest request);

    List<CommunityTopicResponse> listTopics();

    CommunityTopicResponse createTopic(CommunityTopicRequest request);
}