package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommunityCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论长度不能超过1000")
    private String content;

    private Boolean relayReply;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getRelayReply() {
        return relayReply;
    }

    public void setRelayReply(Boolean relayReply) {
        this.relayReply = relayReply;
    }
}
