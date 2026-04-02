package com.pet.memorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "community_comments")
public class CommunityComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(nullable = false, length = 100)
    private String authorUsername;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Boolean relayReply = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean hiddenByAdmin = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommunityPost getPost() {
        return post;
    }

    public void setPost(CommunityPost post) {
        this.post = post;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

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

    public Boolean getHiddenByAdmin() {
        return hiddenByAdmin;
    }

    public void setHiddenByAdmin(Boolean hiddenByAdmin) {
        this.hiddenByAdmin = hiddenByAdmin;
    }
}
