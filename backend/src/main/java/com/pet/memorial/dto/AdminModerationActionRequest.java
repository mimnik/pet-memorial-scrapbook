package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminModerationActionRequest {

    @NotBlank(message = "审核动作不能为空")
    @Size(max = 20, message = "审核动作长度不能超过20")
    private String action;

    @Size(max = 1000, message = "审核原因长度不能超过1000")
    private String reason;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
