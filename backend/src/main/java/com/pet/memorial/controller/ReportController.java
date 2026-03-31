package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.ReportCreateRequest;
import com.pet.memorial.dto.ReportHandleRequest;
import com.pet.memorial.dto.ReportResponse;
import com.pet.memorial.service.ReportService;
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
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ApiResponse<ReportResponse> create(@Valid @RequestBody ReportCreateRequest request) {
        return ApiResponse.success("举报提交成功", reportService.createReport(request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<ReportResponse>> mine() {
        return ApiResponse.success(reportService.listMine());
    }

    @GetMapping("/admin")
    public ApiResponse<List<ReportResponse>> adminList(@RequestParam(required = false) String status) {
        return ApiResponse.success(reportService.listForAdmin(status));
    }

    @PutMapping("/admin/{id}/handle")
    public ApiResponse<ReportResponse> handle(@PathVariable Long id, @Valid @RequestBody ReportHandleRequest request) {
        return ApiResponse.success("举报处理成功", reportService.handleReport(id, request));
    }
}
