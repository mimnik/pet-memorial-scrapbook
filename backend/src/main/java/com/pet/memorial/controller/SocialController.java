package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.FollowStatusResponse;
import com.pet.memorial.dto.FollowSummaryResponse;
import com.pet.memorial.dto.FollowUserResponse;
import com.pet.memorial.service.SocialService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @PostMapping("/follow/{username}")
    public ApiResponse<FollowStatusResponse> follow(@PathVariable String username) {
        return ApiResponse.success("关注成功", socialService.follow(username));
    }

    @DeleteMapping("/follow/{username}")
    public ApiResponse<FollowStatusResponse> unfollow(@PathVariable String username) {
        return ApiResponse.success("已取消关注", socialService.unfollow(username));
    }

    @GetMapping("/following")
    public ApiResponse<List<FollowUserResponse>> following() {
        return ApiResponse.success(socialService.listFollowing());
    }

    @GetMapping("/followers")
    public ApiResponse<List<FollowUserResponse>> followers() {
        return ApiResponse.success(socialService.listFollowers());
    }

    @GetMapping("/summary")
    public ApiResponse<FollowSummaryResponse> summary() {
        return ApiResponse.success(socialService.getMySummary());
    }
}
