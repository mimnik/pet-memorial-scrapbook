package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReportCreateRequest {

    @NotBlank(message = "举报目标类型不能为空")
    @Size(max = 20, message = "举报目标类型长度不能超过20")
    private String targetType;

    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 120, message = "举报原因长度不能超过120")
    private String reason;

    @Size(max = 2000, message = "举报说明长度不能超过2000")
    private String details;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
