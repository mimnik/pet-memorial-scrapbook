package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountAppealPublicCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名长度不能超过100")
    private String username;

    @NotBlank(message = "申诉类型不能为空")
    @Size(max = 40, message = "申诉类型长度不能超过40")
    private String appealType;

    @NotBlank(message = "申诉详情不能为空")
    @Size(max = 2000, message = "申诉详情长度不能超过2000")
    private String details;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
