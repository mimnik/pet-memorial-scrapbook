package com.pet.memorial.dto;

import jakarta.validation.constraints.Size;

public class AdminUserWarnRequest {

    @Size(max = 1000, message = "警告备注长度不能超过1000")
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
