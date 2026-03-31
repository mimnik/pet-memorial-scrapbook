package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportHandleRequest {

    @NotBlank(message = "处理状态不能为空")
    @Size(max = 20, message = "处理状态长度不能超过20")
    private String status;

    @Size(max = 1000, message = "处理说明长度不能超过1000")
    private String handleNote;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandleNote() {
        return handleNote;
    }

    public void setHandleNote(String handleNote) {
        this.handleNote = handleNote;
    }
}
