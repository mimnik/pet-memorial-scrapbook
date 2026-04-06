package com.pet.memorial.service;

import com.pet.memorial.dto.UserProfileResponse;
import com.pet.memorial.dto.UserProfileUpdateRequest;
import com.pet.memorial.entity.User;
import com.pet.memorial.repository.UserFollowRepository;
import com.pet.memorial.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserRepository userRepository, UserFollowRepository userFollowRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(com.pet.memorial.dto.PasswordChangeRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return toProfileResponse(user);
    }

    public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        user.setDisplayName(trimToNull(request.getDisplayName()));
        user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        user.setBio(trimToNull(request.getBio()));

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private UserProfileResponse toProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setDisplayName(user.getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setBio(user.getBio());
        response.setFollowingCount(userFollowRepository.countByFollowerUsername(user.getUsername()));
        response.setFollowerCount(userFollowRepository.countByFollowingUsername(user.getUsername()));
        return response;
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
