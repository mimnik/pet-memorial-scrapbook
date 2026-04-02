package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountAppealCreateRequest {

    @NotBlank(message = "申诉类型不能为空")
    @Size(max = 40, message = "申诉类型长度不能超过40")
    private String appealType;

    @NotBlank(message = "申诉详情不能为空")
    @Size(max = 2000, message = "申诉详情长度不能超过2000")
    private String details;

    public String getAppealType() {
        return appealType;
    }

    public void setAppealType(String appealType) {
        this.appealType = appealType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
