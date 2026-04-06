package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.AccountAppealHandleRequest;
import com.pet.memorial.dto.AccountAppealResponse;
import com.pet.memorial.dto.AdminCommunityCommentResponse;
import com.pet.memorial.dto.AdminCommunityPostResponse;
import com.pet.memorial.dto.AdminDashboardOverviewResponse;
import com.pet.memorial.dto.AdminModerationActionRequest;
import com.pet.memorial.dto.AdminUserResponse;
import com.pet.memorial.dto.AdminUserStatusUpdateRequest;
import com.pet.memorial.dto.AdminUserWarnRequest;
import com.pet.memorial.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> listUsers(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.listUsers(keyword));
    }

    @PostMapping("/users")
    public ApiResponse<AdminUserResponse> addUser(@Valid @RequestBody com.pet.memorial.dto.AdminAddUserRequest request) {
        return ApiResponse.success("添加用户成功", adminService.addUser(request));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<AdminUserResponse> updateUserStatus(@PathVariable Long id,
                                                           @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return ApiResponse.success("账号状态更新成功", adminService.updateUserStatus(id, request));
    }

    @PostMapping("/users/{id}/warn")
    public ApiResponse<AdminUserResponse> warnUser(@PathVariable Long id,
                                                   @Valid @RequestBody AdminUserWarnRequest request) {
        return ApiResponse.success("警告处理成功", adminService.warnUser(id, request));
    }

    @GetMapping("/account-appeals")
    public ApiResponse<List<AccountAppealResponse>> listAppeals(@RequestParam(required = false) String status) {
        return ApiResponse.success(adminService.listAppealsForAdmin(status));
    }

    @PutMapping("/account-appeals/{id}/handle")
    public ApiResponse<AccountAppealResponse> handleAppeal(@PathVariable Long id,
                                                           @Valid @RequestBody AccountAppealHandleRequest request) {
        return ApiResponse.success("申诉处理成功", adminService.handleAppeal(id, request));
    }

    @GetMapping("/community/posts")
    public ApiResponse<List<AdminCommunityPostResponse>> listPosts(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.listCommunityPosts(keyword));
    }

    @GetMapping("/community/comments")
    public ApiResponse<List<AdminCommunityCommentResponse>> listComments(@RequestParam(required = false) Long postId,
                                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.listCommunityComments(postId, keyword));
    }

    @PutMapping("/community/posts/{id}/moderate")
    public ApiResponse<AdminCommunityPostResponse> moderatePost(@PathVariable Long id,
                                                                 @Valid @RequestBody AdminModerationActionRequest request) {
        return ApiResponse.success("帖子审核处理成功", adminService.moderatePost(id, request));
    }

    @PutMapping("/community/comments/{id}/moderate")
    public ApiResponse<AdminCommunityCommentResponse> moderateComment(@PathVariable Long id,
                                                                       @Valid @RequestBody AdminModerationActionRequest request) {
        return ApiResponse.success("评论审核处理成功", adminService.moderateComment(id, request));
    }

    @GetMapping("/dashboard/overview")
    public ApiResponse<AdminDashboardOverviewResponse> dashboardOverview() {
        return ApiResponse.success(adminService.getDashboardOverview());
    }
}
