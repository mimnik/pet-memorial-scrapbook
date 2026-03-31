package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageRequest {

    @NotBlank(message = "私信内容不能为空")
    @Size(max = 2000, message = "私信内容长度不能超过2000")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
