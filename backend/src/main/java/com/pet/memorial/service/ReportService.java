package com.pet.memorial.service;

import com.pet.memorial.dto.ReportCreateRequest;
import com.pet.memorial.dto.ReportHandleRequest;
import com.pet.memorial.dto.ReportResponse;
import com.pet.memorial.entity.ContentReport;
import com.pet.memorial.entity.User;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.CommunityCommentRepository;
import com.pet.memorial.repository.CommunityPostRepository;
import com.pet.memorial.repository.ContentReportRepository;
import com.pet.memorial.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ReportService {

    private static final Set<String> SUPPORTED_TARGET_TYPES = Set.of("POST", "COMMENT");
    private static final Set<String> SUPPORTED_HANDLE_STATUS = Set.of("RESOLVED", "REJECTED");

    private final ContentReportRepository contentReportRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserRepository userRepository;

    public ReportService(ContentReportRepository contentReportRepository,
                         CommunityPostRepository communityPostRepository,
                         CommunityCommentRepository communityCommentRepository,
                         UserRepository userRepository) {
        this.contentReportRepository = contentReportRepository;
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.userRepository = userRepository;
    }

    public ReportResponse createReport(ReportCreateRequest request) {
        String currentUsername = getCurrentUsername();
        String targetType = normalizeTargetType(request.getTargetType());

        validateTargetExists(targetType, request.getTargetId());

        ContentReport report = new ContentReport();
        report.setReporterUsername(currentUsername);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        report.setDetails(trimToNull(request.getDetails()));
        report.setStatus("PENDING");

        ContentReport saved = contentReportRepository.save(report);
        return toReportResponse(saved);
    }

    public List<ReportResponse> listMine() {
        String currentUsername = getCurrentUsername();
        return contentReportRepository.findByReporterUsernameOrderByCreatedAtDescIdDesc(currentUsername).stream()
            .map(this::toReportResponse)
            .toList();
    }

    public List<ReportResponse> listForAdmin(String status) {
        User currentUser = getCurrentUser();
        ensureAdmin(currentUser);

        String normalizedStatus = normalizeStatusFilter(status);
        List<ContentReport> reports;
        if (normalizedStatus == null) {
            reports = contentReportRepository.findAllByOrderByCreatedAtDescIdDesc();
        } else {
            reports = contentReportRepository.findByStatusOrderByCreatedAtDescIdDesc(normalizedStatus);
        }

        return reports.stream().map(this::toReportResponse).toList();
    }

    public ReportResponse handleReport(Long reportId, ReportHandleRequest request) {
        User currentUser = getCurrentUser();
        ensureAdmin(currentUser);

        ContentReport report = contentReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到举报记录，id=" + reportId));

        String status = normalizeHandleStatus(request.getStatus());
        report.setStatus(status);
        report.setHandledByUsername(currentUser.getUsername());
        report.setHandledAt(LocalDateTime.now());
        report.setHandleNote(trimToNull(request.getHandleNote()));

        ContentReport saved = contentReportRepository.save(report);
        return toReportResponse(saved);
    }

    private ReportResponse toReportResponse(ContentReport report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterUsername(report.getReporterUsername());
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setReason(report.getReason());
        response.setDetails(report.getDetails());
        response.setStatus(report.getStatus());
        response.setHandledByUsername(report.getHandledByUsername());
        response.setHandledAt(report.getHandledAt());
        response.setHandleNote(report.getHandleNote());
        response.setCreatedAt(report.getCreatedAt());
        return response;
    }

    private void validateTargetExists(String targetType, Long targetId) {
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("举报目标ID无效");
        }

        boolean exists = switch (targetType) {
            case "POST" -> communityPostRepository.existsById(targetId);
            case "COMMENT" -> communityCommentRepository.existsById(targetId);
            default -> false;
        };

        if (!exists) {
            throw new ResourceNotFoundException("举报目标不存在");
        }
    }

    private String normalizeTargetType(String value) {
        if (value == null) {
            throw new IllegalArgumentException("举报目标类型不能为空");
        }
        String normalized = value.trim().toUpperCase();
        if (!SUPPORTED_TARGET_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("不支持的举报目标类型: " + value);
        }
        return normalized;
    }

    private String normalizeStatusFilter(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return null;
        }
        if ("PENDING".equals(normalized) || SUPPORTED_HANDLE_STATUS.contains(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("不支持的状态筛选: " + value);
    }

    private String normalizeHandleStatus(String value) {
        if (value == null) {
            throw new IllegalArgumentException("处理状态不能为空");
        }
        String normalized = value.trim().toUpperCase();
        if (!SUPPORTED_HANDLE_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("处理状态仅支持 RESOLVED 或 REJECTED");
        }
        return normalized;
    }

    private void ensureAdmin(User user) {
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("仅管理员可查看和处理举报");
        }
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
