package com.pet.memorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "user_messages",
    indexes = {
        @Index(name = "idx_user_messages_sender_receiver", columnList = "sender_username, receiver_username, created_at"),
        @Index(name = "idx_user_messages_receiver_read", columnList = "receiver_username, read_by_receiver")
    }
)
public class UserMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String senderUsername;

    @Column(nullable = false, length = 100)
    private String receiverUsername;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Boolean readByReceiver = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getReadByReceiver() {
        return readByReceiver;
    }

    public void setReadByReceiver(Boolean readByReceiver) {
        this.readByReceiver = readByReceiver;
    }
}
