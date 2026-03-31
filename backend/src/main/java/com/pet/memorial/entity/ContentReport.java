package com.pet.memorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "content_reports",
    indexes = {
        @Index(name = "idx_content_reports_status", columnList = "status"),
        @Index(name = "idx_content_reports_reporter", columnList = "reporter_username")
    }
)
public class ContentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String reporterUsername;

    @Column(nullable = false, length = 20)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 120)
    private String reason;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 100)
    private String handledByUsername;

    private LocalDateTime handledAt;

    @Column(length = 1000)
    private String handleNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandledByUsername() {
        return handledByUsername;
    }

    public void setHandledByUsername(String handledByUsername) {
        this.handledByUsername = handledByUsername;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public String getHandleNote() {
        return handleNote;
    }

    public void setHandleNote(String handleNote) {
        this.handleNote = handleNote;
    }
}
