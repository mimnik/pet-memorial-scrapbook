package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.MessageConversationResponse;
import com.pet.memorial.dto.MessageRequest;
import com.pet.memorial.dto.MessageResponse;
import com.pet.memorial.dto.MessageSummaryResponse;
import com.pet.memorial.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ApiResponse<List<MessageConversationResponse>> conversations() {
        return ApiResponse.success(messageService.listConversations());
    }

    @GetMapping("/with/{username}")
    public ApiResponse<List<MessageResponse>> conversationWith(@PathVariable String username) {
        return ApiResponse.success(messageService.listConversationWith(username));
    }

    @PostMapping("/with/{username}")
    public ApiResponse<MessageResponse> sendMessage(@PathVariable String username, @Valid @RequestBody MessageRequest request) {
        return ApiResponse.success("发送成功", messageService.sendMessage(username, request));
    }

    @PutMapping("/with/{username}/read")
    public ApiResponse<Void> markRead(@PathVariable String username) {
        messageService.markConversationRead(username);
        return ApiResponse.success("已标记为已读", null);
    }

    @GetMapping("/summary")
    public ApiResponse<MessageSummaryResponse> summary() {
        return ApiResponse.success(messageService.getSummary());
    }
}
