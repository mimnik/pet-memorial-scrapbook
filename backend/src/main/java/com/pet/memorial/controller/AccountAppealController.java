package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.AccountAppealCreateRequest;
import com.pet.memorial.dto.AccountAppealPublicCreateRequest;
import com.pet.memorial.dto.AccountAppealResponse;
import com.pet.memorial.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account-appeals")
public class AccountAppealController {

    private final AdminService adminService;

    public AccountAppealController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ApiResponse<AccountAppealResponse> create(@Valid @RequestBody AccountAppealCreateRequest request) {
        return ApiResponse.success("申诉提交成功", adminService.createAppeal(request));
    }

    @PostMapping("/public")
    public ApiResponse<AccountAppealResponse> createPublic(@Valid @RequestBody AccountAppealPublicCreateRequest request) {
        return ApiResponse.success("申诉提交成功", adminService.createAppealForFrozenUser(request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<AccountAppealResponse>> mine() {
        return ApiResponse.success(adminService.listMyAppeals());
    }
}
