package com.pet.memorial.service;

import com.pet.memorial.dto.CommunityCommentRequest;
import com.pet.memorial.dto.CommunityCommentResponse;
import com.pet.memorial.dto.CommunityPostRequest;
import com.pet.memorial.dto.CommunityPostResponse;
import com.pet.memorial.dto.CommunityTopicRequest;
import com.pet.memorial.dto.CommunityTopicResponse;
import com.pet.memorial.dto.PetHotRankResponse;
import com.pet.memorial.entity.CommunityComment;
import com.pet.memorial.entity.CommunityPost;
import com.pet.memorial.entity.CommunityPostLike;
import com.pet.memorial.entity.CommunityTopic;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.CommunityCommentRepository;
import com.pet.memorial.repository.CommunityPostLikeRepository;
import com.pet.memorial.repository.CommunityPostRepository;
import com.pet.memorial.repository.CommunityTopicRepository;
import com.pet.memorial.repository.PetRepository;
import com.pet.memorial.repository.UserFollowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommunityService implements CommunityOperations {

    private static final Set<String> SUPPORTED_MOODS = Set.of("SUNNY", "CLOUDY", "RAINY", "STORMY", "RAINBOW");
    private static final Set<String> SUPPORTED_MODES = Set.of("DAILY", "MEMORIAL");

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityTopicRepository communityTopicRepository;
    private final UserFollowRepository userFollowRepository;
    private final PetRepository petRepository;

    public CommunityService(CommunityPostRepository communityPostRepository,
                            CommunityCommentRepository communityCommentRepository,
                            CommunityPostLikeRepository communityPostLikeRepository,
                            CommunityTopicRepository communityTopicRepository,
                            UserFollowRepository userFollowRepository,
                            PetRepository petRepository) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.communityPostLikeRepository = communityPostLikeRepository;
        this.communityTopicRepository = communityTopicRepository;
        this.userFollowRepository = userFollowRepository;
        this.petRepository = petRepository;
    }

    public List<CommunityPostResponse> listFeed(String narrativeMode, String moodTag, String keyword, Long topicId) {
        String mode = normalizeModeOrNull(narrativeMode);
        String mood = normalizeMoodOrNull(moodTag);
        String keywordNormalized = trimToNull(keyword);
        Long safeTopicId = normalizeTopicIdForFilter(topicId);

        String currentUsername = getCurrentUsername();
        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);

        return communityPostRepository.findFeed(mode, mood, keywordNormalized, safeTopicId).stream()
            .map(post -> toPostResponse(post, currentUsername, followingUsernames))
            .toList();
    }

    public List<CommunityPostResponse> listFollowingFeed() {
        String currentUsername = getCurrentUsername();
        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);
        if (followingUsernames.isEmpty()) {
            return List.of();
        }

        List<CommunityPost> posts = communityPostRepository.findByAuthorUsernameInOrderByCreatedAtDescIdDesc(
            new ArrayList<>(followingUsernames)
        );

        return posts.stream()
            .map(post -> toPostResponse(post, currentUsername, followingUsernames))
            .toList();
    }

    public List<CommunityPostResponse> listRecommendations(int limit) {
        int safeLimit = normalizeLimit(limit, 6, 20);
        String currentUsername = getCurrentUsername();
        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);

        List<CommunityPost> allPosts = communityPostRepository.findAllByOrderByCreatedAtDescIdDesc();
        List<CommunityPostResponse> result = new ArrayList<>();
        for (CommunityPost post : allPosts) {
            CommunityPostResponse response = toPostResponse(post, currentUsername, followingUsernames);
            response.setRecommendationScore(calculateRecommendationScore(post));
            result.add(response);
        }

        return result.stream()
            .sorted(Comparator.comparing(CommunityPostResponse::getRecommendationScore).reversed())
            .limit(safeLimit)
            .toList();
    }

    public List<PetHotRankResponse> listHotPets(int limit) {
        int safeLimit = normalizeLimit(limit, 10, 20);
        List<CommunityPost> allPosts = communityPostRepository.findAllByOrderByCreatedAtDescIdDesc();

        Map<Long, PetHotRankResponse> rankings = new HashMap<>();
        for (CommunityPost post : allPosts) {
            Pet pet = post.getPet();
            PetHotRankResponse row = rankings.computeIfAbsent(pet.getId(), key -> {
                PetHotRankResponse init = new PetHotRankResponse();
                init.setPetId(pet.getId());
                init.setPetName(pet.getName());
                init.setPetAvatarUrl(pet.getAvatarUrl());
                init.setOwnerUsername(pet.getOwnerUsername());
                init.setPostCount(0);
                init.setTotalLikes(0);
                init.setTotalComments(0);
                init.setHeatScore(0D);
                return init;
            });

            row.setPostCount(row.getPostCount() + 1);
            row.setTotalLikes(row.getTotalLikes() + safeInt(post.getLikeCount()));
            row.setTotalComments(row.getTotalComments() + safeInt(post.getCommentCount()));

            double postScore = safeInt(post.getLikeCount()) * 2.5 + safeInt(post.getCommentCount()) * 3.0;
            if (Boolean.TRUE.equals(post.getPetVoice())) {
                postScore += 1.2;
            }
            if (Boolean.TRUE.equals(post.getRelayEnabled())) {
                postScore += 1.0;
            }
            row.setHeatScore(row.getHeatScore() + postScore);
        }

        return rankings.values().stream()
            .sorted(Comparator.comparing(PetHotRankResponse::getHeatScore).reversed())
            .limit(safeLimit)
            .toList();
    }

    public List<CommunityPostResponse> listMine() {
        String currentUsername = getCurrentUsername();
        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);

        return communityPostRepository.findByAuthorUsernameOrderByCreatedAtDescIdDesc(currentUsername).stream()
            .map(post -> toPostResponse(post, currentUsername, followingUsernames))
            .toList();
    }

    public CommunityPostResponse createPost(CommunityPostRequest request) {
        String currentUsername = getCurrentUsername();
        Pet pet = petRepository.findByIdAndOwnerUsername(request.getPetId(), currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("未找到可发布的宠物，id=" + request.getPetId()));

        CommunityTopic topic = null;
        Long topicId = normalizeTopicIdForCreate(request.getTopicId());
        if (topicId != null) {
            topic = communityTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到话题，id=" + topicId));
        }

        CommunityPost post = new CommunityPost();
        post.setPet(pet);
        post.setTopic(topic);
        post.setAuthorUsername(currentUsername);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setImageUrl(trimToNull(request.getImageUrl()));
        post.setVideoUrl(trimToNull(request.getVideoUrl()));
        post.setVideoCoverUrl(trimToNull(request.getVideoCoverUrl()));
        post.setVideoDurationSeconds(normalizeDuration(request.getVideoDurationSeconds()));
        post.setMoodTag(normalizeMoodOrDefault(request.getMoodTag()));
        post.setNarrativeMode(normalizeModeOrDefault(request.getNarrativeMode()));
        post.setPetVoice(Boolean.TRUE.equals(request.getPetVoice()));
        post.setRelayEnabled(Boolean.TRUE.equals(request.getRelayEnabled()));
        post.setLikeCount(0);
        post.setCommentCount(0);

        CommunityPost saved = communityPostRepository.save(post);
        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);
        return toPostResponse(saved, currentUsername, followingUsernames);
    }

    public CommunityPostResponse toggleLike(Long postId) {
        String currentUsername = getCurrentUsername();
        CommunityPost post = getPost(postId);

        communityPostLikeRepository.findByPostIdAndUsername(postId, currentUsername)
            .ifPresentOrElse(
                communityPostLikeRepository::delete,
                () -> {
                    CommunityPostLike like = new CommunityPostLike();
                    like.setPost(post);
                    like.setUsername(currentUsername);
                    communityPostLikeRepository.save(like);
                }
            );

        long likeCount = communityPostLikeRepository.countByPostId(postId);
        post.setLikeCount((int) likeCount);
        CommunityPost saved = communityPostRepository.save(post);

        Set<String> followingUsernames = loadFollowingUsernameSet(currentUsername);
        return toPostResponse(saved, currentUsername, followingUsernames);
    }

    public List<CommunityCommentResponse> listComments(Long postId) {
        getPost(postId);
        return communityCommentRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId).stream()
            .map(this::toCommentResponse)
            .toList();
    }

    public CommunityCommentResponse createComment(Long postId, CommunityCommentRequest request) {
        String currentUsername = getCurrentUsername();
        CommunityPost post = getPost(postId);

        CommunityComment comment = new CommunityComment();
        comment.setPost(post);
        comment.setAuthorUsername(currentUsername);
        comment.setContent(request.getContent().trim());

        boolean relayReply = Boolean.TRUE.equals(request.getRelayReply()) && Boolean.TRUE.equals(post.getRelayEnabled());
        comment.setRelayReply(relayReply);

        CommunityComment saved = communityCommentRepository.save(comment);

        long commentCount = communityCommentRepository.countByPostId(postId);
        post.setCommentCount((int) commentCount);
        communityPostRepository.save(post);

        return toCommentResponse(saved);
    }

    public List<CommunityTopicResponse> listTopics() {
        return communityTopicRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
            .map(this::toTopicResponse)
            .toList();
    }

    public CommunityTopicResponse createTopic(CommunityTopicRequest request) {
        String currentUsername = getCurrentUsername();
        String topicName = normalizeTopicName(request.getName());
        if (communityTopicRepository.findByNameIgnoreCase(topicName).isPresent()) {
            throw new IllegalArgumentException("话题已存在");
        }

        CommunityTopic topic = new CommunityTopic();
        topic.setName(topicName);
        topic.setDescription(trimToNull(request.getDescription()));
        topic.setCreatedByUsername(currentUsername);

        CommunityTopic saved = communityTopicRepository.save(topic);
        return toTopicResponse(saved);
    }

    private CommunityPost getPost(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("帖子ID不能为空");
        }
        return communityPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到社区帖子，id=" + postId));
    }

    private CommunityPostResponse toPostResponse(CommunityPost post, String currentUsername, Set<String> followingUsernames) {
        CommunityPostResponse response = new CommunityPostResponse();
        response.setId(post.getId());
        response.setPetId(post.getPet().getId());
        response.setPetName(post.getPet().getName());
        response.setPetAvatarUrl(post.getPet().getAvatarUrl());
        if (post.getTopic() != null) {
            response.setTopicId(post.getTopic().getId());
            response.setTopicName(post.getTopic().getName());
        }
        response.setAuthorUsername(post.getAuthorUsername());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setVideoUrl(post.getVideoUrl());
        response.setVideoCoverUrl(post.getVideoCoverUrl());
        response.setVideoDurationSeconds(post.getVideoDurationSeconds());
        response.setMoodTag(post.getMoodTag());
        response.setNarrativeMode(post.getNarrativeMode());
        response.setPetVoice(post.getPetVoice());
        response.setRelayEnabled(post.getRelayEnabled());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setLikedByMe(communityPostLikeRepository.existsByPostIdAndUsername(post.getId(), currentUsername));
        boolean followed = !post.getAuthorUsername().equals(currentUsername)
            && followingUsernames.contains(post.getAuthorUsername());
        response.setAuthorFollowedByMe(followed);
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setRecommendationScore(calculateRecommendationScore(post));
        return response;
    }

    private CommunityCommentResponse toCommentResponse(CommunityComment comment) {
        CommunityCommentResponse response = new CommunityCommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getId());
        response.setAuthorUsername(comment.getAuthorUsername());
        response.setContent(comment.getContent());
        response.setRelayReply(comment.getRelayReply());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private CommunityTopicResponse toTopicResponse(CommunityTopic topic) {
        CommunityTopicResponse response = new CommunityTopicResponse();
        response.setId(topic.getId());
        response.setName(topic.getName());
        response.setDescription(topic.getDescription());
        response.setCreatedByUsername(topic.getCreatedByUsername());
        response.setCreatedAt(topic.getCreatedAt());
        return response;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }

    private Set<String> loadFollowingUsernameSet(String username) {
        List<String> followingUsernames = userFollowRepository.findFollowingUsernamesByFollowerUsername(username);
        if (followingUsernames.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(followingUsernames);
    }

    private String normalizeMoodOrDefault(String moodTag) {
        String value = normalizeMoodOrNull(moodTag);
        return value == null ? "SUNNY" : value;
    }

    private String normalizeModeOrDefault(String narrativeMode) {
        String value = normalizeModeOrNull(narrativeMode);
        return value == null ? "DAILY" : value;
    }

    private String normalizeMoodOrNull(String moodTag) {
        String value = trimToNull(moodTag);
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase();
        if (!SUPPORTED_MOODS.contains(normalized)) {
            throw new IllegalArgumentException("不支持的情绪标签: " + value);
        }
        return normalized;
    }

    private String normalizeModeOrNull(String narrativeMode) {
        String value = trimToNull(narrativeMode);
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase();
        if (!SUPPORTED_MODES.contains(normalized)) {
            throw new IllegalArgumentException("不支持的叙事模式: " + value);
        }
        return normalized;
    }

    private String normalizeTopicName(String topicName) {
        String value = trimToNull(topicName);
        if (value == null) {
            throw new IllegalArgumentException("话题名称不能为空");
        }
        return value;
    }

    private Long normalizeTopicIdForFilter(Long topicId) {
        if (topicId == null || topicId <= 0) {
            return null;
        }
        return topicId;
    }

    private Long normalizeTopicIdForCreate(Long topicId) {
        if (topicId == null) {
            return null;
        }
        if (topicId <= 0) {
            throw new IllegalArgumentException("话题ID无效");
        }
        return topicId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int normalizeLimit(int value, int defaultLimit, int maxLimit) {
        if (value <= 0) {
            return defaultLimit;
        }
        return Math.min(value, maxLimit);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer normalizeDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            return null;
        }
        return Math.min(duration, 24 * 60 * 60);
    }

    // Custom recommendation score for graduation-design innovation:
    // interaction + freshness + narrative-experiment bonus + mood diversity bonus.
    private double calculateRecommendationScore(CommunityPost post) {
        int like = safeInt(post.getLikeCount());
        int comment = safeInt(post.getCommentCount());

        LocalDateTime createdAt = post.getCreatedAt() == null ? LocalDateTime.now() : post.getCreatedAt();
        long hours = Math.max(1, Duration.between(createdAt, LocalDateTime.now()).toHours() + 1);

        double engagementScore = like * 2.8 + comment * 3.4;
        double freshnessScore = 16.0 / Math.log(hours + 2.0);
        double narrativeBonus = Boolean.TRUE.equals(post.getPetVoice()) ? 2.2 : 0.0;
        narrativeBonus += Boolean.TRUE.equals(post.getRelayEnabled()) ? 1.8 : 0.0;
        narrativeBonus += "MEMORIAL".equalsIgnoreCase(post.getNarrativeMode()) ? 1.0 : 0.5;

        double moodBonus = switch (post.getMoodTag() == null ? "" : post.getMoodTag()) {
            case "RAINBOW" -> 1.2;
            case "STORMY" -> 0.8;
            case "RAINY" -> 0.6;
            default -> 0.4;
        };

        return Math.round((engagementScore + freshnessScore + narrativeBonus + moodBonus) * 100.0) / 100.0;
    }
}
