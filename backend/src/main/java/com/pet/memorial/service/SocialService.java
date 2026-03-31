package com.pet.memorial.service;

import com.pet.memorial.dto.FollowStatusResponse;
import com.pet.memorial.dto.FollowSummaryResponse;
import com.pet.memorial.dto.FollowUserResponse;
import com.pet.memorial.entity.User;
import com.pet.memorial.entity.UserFollow;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.UserFollowRepository;
import com.pet.memorial.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public SocialService(UserFollowRepository userFollowRepository, UserRepository userRepository) {
        this.userFollowRepository = userFollowRepository;
        this.userRepository = userRepository;
    }

    public FollowStatusResponse follow(String targetUsername) {
        String currentUsername = getCurrentUsername();
        String target = normalizeUsername(targetUsername);
        validateFollowTarget(currentUsername, target);

        if (!userFollowRepository.existsByFollowerUsernameAndFollowingUsername(currentUsername, target)) {
            UserFollow follow = new UserFollow();
            follow.setFollowerUsername(currentUsername);
            follow.setFollowingUsername(target);
            userFollowRepository.save(follow);
        }

        return buildFollowStatus(target, true);
    }

    public FollowStatusResponse unfollow(String targetUsername) {
        String currentUsername = getCurrentUsername();
        String target = normalizeUsername(targetUsername);
        validateFollowTarget(currentUsername, target);

        userFollowRepository.findByFollowerUsernameAndFollowingUsername(currentUsername, target)
            .ifPresent(userFollowRepository::delete);

        return buildFollowStatus(target, false);
    }

    public List<FollowUserResponse> listFollowing() {
        String currentUsername = getCurrentUsername();
        List<UserFollow> follows = userFollowRepository.findByFollowerUsernameOrderByCreatedAtDesc(currentUsername);
        return toFollowUsers(follows, true);
    }

    public List<FollowUserResponse> listFollowers() {
        String currentUsername = getCurrentUsername();
        List<UserFollow> follows = userFollowRepository.findByFollowingUsernameOrderByCreatedAtDesc(currentUsername);
        return toFollowUsers(follows, false);
    }

    public FollowSummaryResponse getMySummary() {
        String currentUsername = getCurrentUsername();
        FollowSummaryResponse summary = new FollowSummaryResponse();
        summary.setFollowingCount(userFollowRepository.countByFollowerUsername(currentUsername));
        summary.setFollowerCount(userFollowRepository.countByFollowingUsername(currentUsername));
        return summary;
    }

    private List<FollowUserResponse> toFollowUsers(List<UserFollow> follows, boolean followingView) {
        if (follows.isEmpty()) {
            return List.of();
        }

        List<String> usernames = follows.stream()
            .map(item -> followingView ? item.getFollowingUsername() : item.getFollowerUsername())
            .toList();

        Map<String, User> usersByUsername = new HashMap<>();
        for (User user : userRepository.findByUsernameIn(usernames)) {
            usersByUsername.put(user.getUsername(), user);
        }

        List<FollowUserResponse> result = new ArrayList<>();
        for (UserFollow follow : follows) {
            String username = followingView ? follow.getFollowingUsername() : follow.getFollowerUsername();
            User user = usersByUsername.get(username);

            FollowUserResponse row = new FollowUserResponse();
            row.setUsername(username);
            row.setDisplayName(user == null ? null : user.getDisplayName());
            row.setAvatarUrl(user == null ? null : user.getAvatarUrl());
            row.setBio(user == null ? null : user.getBio());
            row.setFollowedAt(follow.getCreatedAt());
            result.add(row);
        }

        return result;
    }

    private FollowStatusResponse buildFollowStatus(String targetUsername, boolean following) {
        FollowStatusResponse response = new FollowStatusResponse();
        response.setTargetUsername(targetUsername);
        response.setFollowing(following);
        response.setFollowingCount(userFollowRepository.countByFollowerUsername(targetUsername));
        response.setFollowerCount(userFollowRepository.countByFollowingUsername(targetUsername));
        return response;
    }

    private void validateFollowTarget(String currentUsername, String targetUsername) {
        if (currentUsername.equals(targetUsername)) {
            throw new IllegalArgumentException("不能关注自己");
        }
        if (!userRepository.existsByUsername(targetUsername)) {
            throw new ResourceNotFoundException("目标用户不存在");
        }
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return trimmed;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }
}
