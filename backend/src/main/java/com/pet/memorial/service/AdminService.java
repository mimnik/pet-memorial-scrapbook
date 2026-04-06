package com.pet.memorial.service;

import com.pet.memorial.dto.AccountAppealCreateRequest;
import com.pet.memorial.dto.AccountAppealHandleRequest;
import com.pet.memorial.dto.AccountAppealPublicCreateRequest;
import com.pet.memorial.dto.AccountAppealResponse;
import com.pet.memorial.dto.AdminCommunityCommentResponse;
import com.pet.memorial.dto.AdminCommunityPostResponse;
import com.pet.memorial.dto.AdminDashboardOverviewResponse;
import com.pet.memorial.dto.AdminModerationActionRequest;
import com.pet.memorial.dto.AdminUserResponse;
import com.pet.memorial.dto.AdminUserStatusUpdateRequest;
import com.pet.memorial.dto.AdminUserWarnRequest;
import com.pet.memorial.entity.CommunityComment;
import com.pet.memorial.entity.CommunityPost;
import com.pet.memorial.entity.User;
import com.pet.memorial.entity.UserAccountAppeal;
import com.pet.memorial.entity.UserMessage;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.CommunityCommentRepository;
import com.pet.memorial.repository.CommunityPostRepository;
import com.pet.memorial.repository.CommunityTopicRepository;
import com.pet.memorial.repository.MemoryEntryRepository;
import com.pet.memorial.repository.UserAccountAppealRepository;
import com.pet.memorial.repository.UserMessageRepository;
import com.pet.memorial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class AdminService {

    private static final Set<String> APPEAL_STATUS = Set.of("PENDING", "PROCESSING", "RESOLVED", "REJECTED");
    private static final Set<String> APPEAL_HANDLE_STATUS = Set.of("PROCESSING", "RESOLVED", "REJECTED");
    private static final Set<String> MODERATION_ACTIONS = Set.of("DELETE", "SHIELD");

    private final UserRepository userRepository;
    private final UserAccountAppealRepository appealRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final MemoryEntryRepository memoryEntryRepository;
    private final CommunityTopicRepository communityTopicRepository;
    private final UserMessageRepository userMessageRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public AdminService(UserRepository userRepository,
                        UserAccountAppealRepository appealRepository,
                        CommunityPostRepository communityPostRepository,        
                        CommunityCommentRepository communityCommentRepository,  
                        MemoryEntryRepository memoryEntryRepository,
                        CommunityTopicRepository communityTopicRepository,      
                        UserMessageRepository userMessageRepository,
                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appealRepository = appealRepository;
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.memoryEntryRepository = memoryEntryRepository;
        this.communityTopicRepository = communityTopicRepository;
        this.userMessageRepository = userMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public java.util.List<AdminUserResponse> listUsers(String keyword) {
        ensureAdmin();
        String normalizedKeyword = normalizeKeyword(keyword);
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
            .filter(user -> matchesUserKeyword(user, normalizedKeyword))
            .map(this::toAdminUserResponse)
            .toList();
    }

    public AdminUserResponse updateUserStatus(Long userId, AdminUserStatusUpdateRequest request) {
        User admin = ensureAdmin();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在，id=" + userId));

        boolean operatingSelf = user.getUsername().equals(admin.getUsername());
        if (operatingSelf && Boolean.TRUE.equals(request.getAccountFrozen())) {
            throw new IllegalArgumentException("不能冻结当前管理员账号");
        }

        if (request.getAccountFrozen() != null) {
            user.setAccountFrozen(request.getAccountFrozen());
        }
        if (request.getPostingRestricted() != null) {
            user.setPostingRestricted(request.getPostingRestricted());
        }

        String adminNote = trimToNull(request.getAdminNote());
        if (adminNote != null || request.getAdminNote() != null) {
            user.setAdminNote(adminNote);
        }

        User saved = userRepository.save(user);
        return toAdminUserResponse(saved);
    }

    public AdminUserResponse warnUser(Long userId, AdminUserWarnRequest request) {
        User admin = ensureAdmin();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在，id=" + userId));

        int nextWarnings = (user.getWarningCount() == null ? 0 : user.getWarningCount()) + 1;
        user.setWarningCount(nextWarnings);
        if (nextWarnings >= 3) {
            user.setPostingRestricted(Boolean.TRUE);
        }

        String note = request == null ? null : trimToNull(request.getNote());
        if (note != null) {
            user.setAdminNote(note);
        }

        User saved = userRepository.save(user);
        createWarningMessage(admin.getUsername(), saved.getUsername(), note, nextWarnings);
        return toAdminUserResponse(saved);
    }

    private void createWarningMessage(String adminUsername, String targetUsername, String note, int warningCount) {
        UserMessage warningMessage = new UserMessage();
        warningMessage.setSenderUsername(adminUsername);
        warningMessage.setReceiverUsername(targetUsername);
        warningMessage.setContent(buildWarningMessageContent(note, warningCount));
        warningMessage.setReadByReceiver(Boolean.FALSE);
        userMessageRepository.save(warningMessage);
    }

    private String buildWarningMessageContent(String note, int warningCount) {
        StringBuilder content = new StringBuilder("【管理员警告提醒】你收到一条社区行为警告，请遵守社区规范。");
        if (note != null) {
            content.append(" 原因：").append(note).append('。');
        }
        content.append(" 当前累计警告次数：").append(Math.max(warningCount, 0)).append('。');

        if (content.length() > 2000) {
            return content.substring(0, 2000);
        }
        return content.toString();
    }

    public AccountAppealResponse createAppeal(AccountAppealCreateRequest request) {
        String username = getCurrentUsername();
        userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));

        UserAccountAppeal appeal = new UserAccountAppeal();
        appeal.setUsername(username);
        appeal.setAppealType(normalizeAppealType(request.getAppealType()));
        appeal.setDetails(request.getDetails().trim());
        appeal.setStatus("PENDING");

        UserAccountAppeal saved = appealRepository.save(appeal);
        return toAppealResponse(saved);
    }

    public AccountAppealResponse createAppealForFrozenUser(AccountAppealPublicCreateRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("账号不存在"));

        if (!Boolean.TRUE.equals(user.getAccountFrozen())) {
            throw new IllegalArgumentException("当前账号未被冻结，无需提交封禁申诉");
        }

        UserAccountAppeal appeal = new UserAccountAppeal();
        appeal.setUsername(user.getUsername());
        appeal.setAppealType(normalizeAppealType(request.getAppealType()));
        appeal.setDetails(request.getDetails().trim());
        appeal.setStatus("PENDING");

        UserAccountAppeal saved = appealRepository.save(appeal);
        return toAppealResponse(saved);
    }

    public List<AccountAppealResponse> listMyAppeals() {
        String username = getCurrentUsername();
        return appealRepository.findByUsernameOrderByCreatedAtDescIdDesc(username).stream()
            .map(this::toAppealResponse)
            .toList();
    }

    public List<AccountAppealResponse> listAppealsForAdmin(String status) {
        ensureAdmin();
        String normalizedStatus = normalizeAppealStatusFilter(status);

        List<UserAccountAppeal> appeals;
        if (normalizedStatus == null) {
            appeals = appealRepository.findAllByOrderByCreatedAtDescIdDesc();
        } else {
            appeals = appealRepository.findByStatusOrderByCreatedAtDescIdDesc(normalizedStatus);
        }

        return appeals.stream().map(this::toAppealResponse).toList();
    }

    public AccountAppealResponse handleAppeal(Long appealId, AccountAppealHandleRequest request) {
        User admin = ensureAdmin();
        UserAccountAppeal appeal = appealRepository.findById(appealId)
            .orElseThrow(() -> new ResourceNotFoundException("申诉不存在，id=" + appealId));

        String status = normalizeAppealHandleStatus(request.getStatus());
        appeal.setStatus(status);
        appeal.setHandledByUsername(admin.getUsername());
        appeal.setHandledAt(LocalDateTime.now());
        appeal.setHandleNote(trimToNull(request.getHandleNote()));

        UserAccountAppeal saved = appealRepository.save(appeal);
        return toAppealResponse(saved);
    }

    public List<AdminCommunityPostResponse> listCommunityPosts(String keyword) {
        ensureAdmin();
        String normalizedKeyword = normalizeKeyword(keyword);

        return communityPostRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
            .filter(post -> matchesPostKeyword(post, normalizedKeyword))
            .map(this::toAdminPostResponse)
            .toList();
    }

    public List<AdminCommunityCommentResponse> listCommunityComments(Long postId, String keyword) {
        ensureAdmin();
        String normalizedKeyword = normalizeKeyword(keyword);

        List<CommunityComment> comments;
        if (postId != null && postId > 0) {
            comments = communityCommentRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        } else {
            comments = communityCommentRepository.findAllByOrderByCreatedAtDescIdDesc();
        }

        return comments.stream()
            .filter(comment -> matchesCommentKeyword(comment, normalizedKeyword))
            .map(this::toAdminCommentResponse)
            .toList();
    }

    public AdminCommunityPostResponse moderatePost(Long postId, AdminModerationActionRequest request) {
        ensureAdmin();
        String action = normalizeModerationAction(request.getAction());
        String reason = trimToNull(request.getReason());

        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("帖子不存在，id=" + postId));

        if ("DELETE".equals(action)) {
            AdminCommunityPostResponse snapshot = toAdminPostResponse(post);
            communityPostRepository.delete(post);
            return snapshot;
        }

        post.setHiddenByAdmin(Boolean.TRUE);
        post.setTitle("[内容已屏蔽]");
        if (reason == null) {
            post.setContent("该内容因违规已被管理员屏蔽。");
        } else {
            post.setContent("该内容因违规已被管理员屏蔽。原因：" + reason);
        }
        post.setImageUrl(null);
        post.setVideoUrl(null);
        post.setVideoCoverUrl(null);

        CommunityPost saved = communityPostRepository.save(post);
        return toAdminPostResponse(saved);
    }

    public AdminCommunityCommentResponse moderateComment(Long commentId, AdminModerationActionRequest request) {
        ensureAdmin();
        String action = normalizeModerationAction(request.getAction());
        String reason = trimToNull(request.getReason());

        CommunityComment comment = communityCommentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("评论不存在，id=" + commentId));
        CommunityPost post = comment.getPost();

        if ("DELETE".equals(action)) {
            AdminCommunityCommentResponse snapshot = toAdminCommentResponse(comment);
            communityCommentRepository.delete(comment);
            refreshPostCommentCount(post);
            return snapshot;
        }

        comment.setHiddenByAdmin(Boolean.TRUE);
        if (reason == null) {
            comment.setContent("该评论因违规已被管理员屏蔽。");
        } else {
            comment.setContent("该评论因违规已被管理员屏蔽。原因：" + reason);
        }

        CommunityComment saved = communityCommentRepository.save(comment);
        refreshPostCommentCount(post);
        return toAdminCommentResponse(saved);
    }

    public AdminDashboardOverviewResponse getDashboardOverview() {
        ensureAdmin();

        AdminDashboardOverviewResponse response = new AdminDashboardOverviewResponse();
        response.setTotalUsers(userRepository.count());
        response.setFrozenUsers(userRepository.countByAccountFrozenTrue());
        response.setRestrictedUsers(userRepository.countByPostingRestrictedTrue());
        response.setTotalPosts(communityPostRepository.count());
        response.setTotalComments(communityCommentRepository.count());
        response.setTotalMemories(memoryEntryRepository.count());
        response.setTotalTopics(communityTopicRepository.count());

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        response.setUploadDir(uploadPath.toString());
        response.setUploadApiHealthy(checkUploadDirectory(uploadPath));

        long[] mediaStats = calculateUploadStats(uploadPath);
        response.setMultimediaFileCount(mediaStats[0]);
        response.setMultimediaStorageBytes(mediaStats[1]);

        response.setAuthApiHealthy(runHealthCheck(() -> userRepository.count()));
        response.setCommunityApiHealthy(runHealthCheck(() -> {
            communityPostRepository.count();
            communityCommentRepository.count();
        }));

        return response;
    }

    private void refreshPostCommentCount(CommunityPost post) {
        long visibleComments = communityCommentRepository.countByPostIdAndHiddenByAdminFalse(post.getId());
        post.setCommentCount((int) visibleComments);
        communityPostRepository.save(post);
    }

    private boolean matchesUserKeyword(User user, String keyword) {
        if (keyword == null) {
            return true;
        }
        return containsIgnoreCase(user.getUsername(), keyword)
            || containsIgnoreCase(user.getEmail(), keyword)
            || containsIgnoreCase(user.getDisplayName(), keyword);
    }

    private boolean matchesPostKeyword(CommunityPost post, String keyword) {
        if (keyword == null) {
            return true;
        }
        return containsIgnoreCase(post.getAuthorUsername(), keyword)
            || containsIgnoreCase(post.getTitle(), keyword)
            || containsIgnoreCase(post.getContent(), keyword)
            || (post.getPet() != null && containsIgnoreCase(post.getPet().getName(), keyword));
    }

    private boolean matchesCommentKeyword(CommunityComment comment, String keyword) {
        if (keyword == null) {
            return true;
        }
        return containsIgnoreCase(comment.getAuthorUsername(), keyword)
            || containsIgnoreCase(comment.getContent(), keyword)
            || (comment.getPost() != null && containsIgnoreCase(comment.getPost().getTitle(), keyword));
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeKeyword(String keyword) {
        String value = trimToNull(keyword);
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeAppealType(String appealType) {
        String value = trimToNull(appealType);
        if (value == null) {
            throw new IllegalArgumentException("申诉类型不能为空");
        }
        return value.toUpperCase(Locale.ROOT);
    }

    private String normalizeAppealStatusFilter(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!APPEAL_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("不支持的申诉状态: " + value);
        }
        return normalized;
    }

    private String normalizeAppealHandleStatus(String status) {
        String value = trimToNull(status);
        if (value == null) {
            throw new IllegalArgumentException("处理状态不能为空");
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!APPEAL_HANDLE_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("不支持的处理状态: " + value);
        }
        return normalized;
    }

    private String normalizeModerationAction(String action) {
        String value = trimToNull(action);
        if (value == null) {
            throw new IllegalArgumentException("审核动作不能为空");
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!MODERATION_ACTIONS.contains(normalized)) {
            throw new IllegalArgumentException("不支持的审核动作: " + value);
        }
        return normalized;
    }

    private long[] calculateUploadStats(Path uploadPath) {
        if (!Files.exists(uploadPath) || !Files.isDirectory(uploadPath)) {
            return new long[]{0L, 0L};
        }

        long[] result = new long[]{0L, 0L};
        try (Stream<Path> stream = Files.walk(uploadPath)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                result[0] += 1;
                try {
                    result[1] += Files.size(path);
                } catch (IOException ignored) {
                    // Ignore inaccessible files and continue stats scan.
                }
            });
        } catch (IOException ignored) {
            return new long[]{0L, 0L};
        }
        return result;
    }

    private boolean checkUploadDirectory(Path uploadPath) {
        try {
            Files.createDirectories(uploadPath);
            return Files.isDirectory(uploadPath) && Files.isWritable(uploadPath);
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean runHealthCheck(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }

    private User ensureAdmin() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("仅管理员可访问");
        }
        return user;
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setRole(user.getRole());
        response.setAccountFrozen(Boolean.TRUE.equals(user.getAccountFrozen()));
        response.setPostingRestricted(Boolean.TRUE.equals(user.getPostingRestricted()));
        response.setWarningCount(user.getWarningCount() == null ? 0 : user.getWarningCount());
        response.setAdminNote(user.getAdminNote());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private AccountAppealResponse toAppealResponse(UserAccountAppeal appeal) {
        AccountAppealResponse response = new AccountAppealResponse();
        response.setId(appeal.getId());
        response.setUsername(appeal.getUsername());
        response.setAppealType(appeal.getAppealType());
        response.setDetails(appeal.getDetails());
        response.setStatus(appeal.getStatus());
        response.setHandledByUsername(appeal.getHandledByUsername());
        response.setHandledAt(appeal.getHandledAt());
        response.setHandleNote(appeal.getHandleNote());
        response.setCreatedAt(appeal.getCreatedAt());
        return response;
    }

    private AdminCommunityPostResponse toAdminPostResponse(CommunityPost post) {
        AdminCommunityPostResponse response = new AdminCommunityPostResponse();
        response.setId(post.getId());
        response.setAuthorUsername(post.getAuthorUsername());
        response.setPetName(post.getPet() == null ? "-" : post.getPet().getName());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setMoodTag(post.getMoodTag());
        response.setNarrativeMode(post.getNarrativeMode());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setHiddenByAdmin(Boolean.TRUE.equals(post.getHiddenByAdmin()));
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }

    private AdminCommunityCommentResponse toAdminCommentResponse(CommunityComment comment) {
        AdminCommunityCommentResponse response = new AdminCommunityCommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost() == null ? null : comment.getPost().getId());
        response.setPostTitle(comment.getPost() == null ? "-" : comment.getPost().getTitle());
        response.setAuthorUsername(comment.getAuthorUsername());
        response.setContent(comment.getContent());
        response.setRelayReply(Boolean.TRUE.equals(comment.getRelayReply()));
        response.setHiddenByAdmin(Boolean.TRUE.equals(comment.getHiddenByAdmin()));
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    public AdminUserResponse addUser(com.pet.memorial.dto.AdminAddUserRequest request) {
        String username;
        if ("ROLE_ADMIN".equals(request.getRole())) {
            java.util.List<User> admins = userRepository.findByRole("ROLE_ADMIN");
            int maxId = 0;
            for (User admin : admins) {
                try {
                    int id = Integer.parseInt(admin.getUsername());
                    if (id > maxId) {
                        maxId = id;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            username = String.format("%04d", maxId + 1);
        } else {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("普通用户必须填写用户名");
            }
            username = request.getUsername().trim();
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("用户名已被占用");
            }
        }
        
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = username + "@pet-memorial.local";
        } else {
            email = email.trim();
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("邮箱已被占用");
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setDisplayName(username);
        user.setAccountFrozen(false);
        user.setPostingRestricted(false);
        user.setWarningCount(0);
        
        userRepository.save(user);
        return toAdminUserResponse(user);
    }
}
