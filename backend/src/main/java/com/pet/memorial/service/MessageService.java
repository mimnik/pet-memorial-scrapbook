package com.pet.memorial.service;

import com.pet.memorial.dto.MessageConversationResponse;
import com.pet.memorial.dto.MessageRequest;
import com.pet.memorial.dto.MessageResponse;
import com.pet.memorial.dto.MessageSummaryResponse;
import com.pet.memorial.entity.User;
import com.pet.memorial.entity.UserMessage;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.UserMessageRepository;
import com.pet.memorial.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final UserMessageRepository userMessageRepository;
    private final UserRepository userRepository;

    public MessageService(UserMessageRepository userMessageRepository, UserRepository userRepository) {
        this.userMessageRepository = userMessageRepository;
        this.userRepository = userRepository;
    }

    public List<MessageConversationResponse> listConversations() {
        String currentUsername = getCurrentUsername();
        List<UserMessage> allMessages = userMessageRepository
            .findBySenderUsernameOrReceiverUsernameOrderByCreatedAtDescIdDesc(currentUsername, currentUsername);

        Map<String, MessageConversationResponse> conversations = new LinkedHashMap<>();
        Map<String, Integer> unreadByPeer = new HashMap<>();

        for (UserMessage message : allMessages) {
            String peer = currentUsername.equals(message.getSenderUsername())
                ? message.getReceiverUsername()
                : message.getSenderUsername();

            if (currentUsername.equals(message.getReceiverUsername()) && !Boolean.TRUE.equals(message.getReadByReceiver())) {
                unreadByPeer.put(peer, unreadByPeer.getOrDefault(peer, 0) + 1);
            }

            if (!conversations.containsKey(peer)) {
                MessageConversationResponse row = new MessageConversationResponse();
                row.setPeerUsername(peer);
                row.setLastMessage(message.getContent());
                row.setLastMessageAt(message.getCreatedAt());
                row.setUnreadCount(0);
                conversations.put(peer, row);
            }
        }

        List<String> usernames = new ArrayList<>(conversations.keySet());
        Map<String, User> usersByUsername = new HashMap<>();
        if (!usernames.isEmpty()) {
            for (User user : userRepository.findByUsernameIn(usernames)) {
                usersByUsername.put(user.getUsername(), user);
            }
        }

        for (MessageConversationResponse row : conversations.values()) {
            User user = usersByUsername.get(row.getPeerUsername());
            row.setPeerDisplayName(user == null ? null : user.getDisplayName());
            row.setPeerAvatarUrl(user == null ? null : user.getAvatarUrl());
            row.setUnreadCount(unreadByPeer.getOrDefault(row.getPeerUsername(), 0));
        }

        return new ArrayList<>(conversations.values());
    }

    public List<MessageResponse> listConversationWith(String peerUsername) {
        String currentUsername = getCurrentUsername();
        String peer = normalizeAndValidatePeer(currentUsername, peerUsername);

        List<UserMessage> messages = userMessageRepository.findConversation(currentUsername, peer);

        List<UserMessage> unreadMessages = new ArrayList<>();
        List<MessageResponse> result = new ArrayList<>();
        for (UserMessage message : messages) {
            if (currentUsername.equals(message.getReceiverUsername()) && !Boolean.TRUE.equals(message.getReadByReceiver())) {
                message.setReadByReceiver(Boolean.TRUE);
                unreadMessages.add(message);
            }
            result.add(toMessageResponse(message, currentUsername));
        }

        if (!unreadMessages.isEmpty()) {
            userMessageRepository.saveAll(unreadMessages);
        }

        return result;
    }

    public MessageResponse sendMessage(String peerUsername, MessageRequest request) {
        String currentUsername = getCurrentUsername();
        String peer = normalizeAndValidatePeer(currentUsername, peerUsername);

        UserMessage message = new UserMessage();
        message.setSenderUsername(currentUsername);
        message.setReceiverUsername(peer);
        message.setContent(request.getContent().trim());
        message.setReadByReceiver(Boolean.FALSE);

        UserMessage saved = userMessageRepository.save(message);
        return toMessageResponse(saved, currentUsername);
    }

    public void markConversationRead(String peerUsername) {
        String currentUsername = getCurrentUsername();
        String peer = normalizeAndValidatePeer(currentUsername, peerUsername);

        List<UserMessage> messages = userMessageRepository.findConversation(currentUsername, peer);
        List<UserMessage> unreadMessages = messages.stream()
            .filter(message -> currentUsername.equals(message.getReceiverUsername()))
            .filter(message -> !Boolean.TRUE.equals(message.getReadByReceiver()))
            .peek(message -> message.setReadByReceiver(Boolean.TRUE))
            .toList();

        if (!unreadMessages.isEmpty()) {
            userMessageRepository.saveAll(unreadMessages);
        }
    }

    public MessageSummaryResponse getSummary() {
        String currentUsername = getCurrentUsername();
        MessageSummaryResponse summary = new MessageSummaryResponse();
        summary.setUnreadCount(userMessageRepository.countByReceiverUsernameAndReadByReceiverFalse(currentUsername));
        return summary;
    }

    private MessageResponse toMessageResponse(UserMessage message, String currentUsername) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderUsername(message.getSenderUsername());
        response.setReceiverUsername(message.getReceiverUsername());
        response.setContent(message.getContent());
        response.setReadByReceiver(message.getReadByReceiver());
        response.setFromMe(currentUsername.equals(message.getSenderUsername()));
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private String normalizeAndValidatePeer(String currentUsername, String peerUsername) {
        if (peerUsername == null) {
            throw new IllegalArgumentException("目标用户名不能为空");
        }

        String peer = peerUsername.trim();
        if (peer.isEmpty()) {
            throw new IllegalArgumentException("目标用户名不能为空");
        }

        if (currentUsername.equals(peer)) {
            throw new IllegalArgumentException("不能给自己发送私信");
        }

        if (!userRepository.existsByUsername(peer)) {
            throw new ResourceNotFoundException("目标用户不存在");
        }

        return peer;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }
}
