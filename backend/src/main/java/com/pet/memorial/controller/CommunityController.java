package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.CommunityCommentRequest;
import com.pet.memorial.dto.CommunityCommentResponse;
import com.pet.memorial.dto.CommunityPostRequest;
import com.pet.memorial.dto.CommunityPostResponse;
import com.pet.memorial.dto.CommunityTopicRequest;
import com.pet.memorial.dto.CommunityTopicResponse;
import com.pet.memorial.dto.PetHotRankResponse;
import com.pet.memorial.service.CommunityOperations;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityOperations communityService;

    public CommunityController(CommunityOperations communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/feed")
    public ApiResponse<List<CommunityPostResponse>> feed(
        @RequestParam(required = false) String mode,
        @RequestParam(required = false) String mood,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long topicId
    ) {
        return ApiResponse.success(communityService.listFeed(mode, mood, keyword, topicId));
    }

    @GetMapping("/following-feed")
    public ApiResponse<List<CommunityPostResponse>> followingFeed() {
        return ApiResponse.success(communityService.listFollowingFeed());
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<CommunityPostResponse>> recommendations(@RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.success(communityService.listRecommendations(limit));
    }

    @GetMapping("/hot-pets")
    public ApiResponse<List<PetHotRankResponse>> hotPets(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(communityService.listHotPets(limit));
    }

    @GetMapping("/mine")
    public ApiResponse<List<CommunityPostResponse>> mine() {
        return ApiResponse.success(communityService.listMine());
    }

    @PostMapping("/posts")
    public ApiResponse<CommunityPostResponse> createPost(@Valid @RequestBody CommunityPostRequest request) {
        return ApiResponse.success("发布成功", communityService.createPost(request));
    }

    @GetMapping("/topics")
    public ApiResponse<List<CommunityTopicResponse>> topics() {
        return ApiResponse.success(communityService.listTopics());
    }

    @PostMapping("/topics")
    public ApiResponse<CommunityTopicResponse> createTopic(@Valid @RequestBody CommunityTopicRequest request) {
        return ApiResponse.success("话题创建成功", communityService.createTopic(request));
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResponse<CommunityPostResponse> toggleLike(@PathVariable Long postId) {
        return ApiResponse.success("点赞状态已更新", communityService.toggleLike(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommunityCommentResponse>> comments(@PathVariable Long postId) {
        return ApiResponse.success(communityService.listComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommunityCommentResponse> createComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommunityCommentRequest request
    ) {
        return ApiResponse.success("评论成功", communityService.createComment(postId, request));
    }
}
