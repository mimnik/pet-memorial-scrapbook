package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.UserProfileResponse;
import com.pet.memorial.dto.UserProfileUpdateRequest;
import com.pet.memorial.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> getMyProfile() {
        return ApiResponse.success(userProfileService.getMyProfile());
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success("个人资料更新成功", userProfileService.updateMyProfile(request));
    }
}
