package com.pet.memorial.dto;

import jakarta.validation.constraints.Size;

public class AdminUserStatusUpdateRequest {

    private Boolean accountFrozen;
    private Boolean postingRestricted;

    @Size(max = 1000, message = "管理员备注长度不能超过1000")
    private String adminNote;

    public Boolean getAccountFrozen() {
        return accountFrozen;
    }

    public void setAccountFrozen(Boolean accountFrozen) {
        this.accountFrozen = accountFrozen;
    }

    public Boolean getPostingRestricted() {
        return postingRestricted;
    }

    public void setPostingRestricted(Boolean postingRestricted) {
        this.postingRestricted = postingRestricted;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}
