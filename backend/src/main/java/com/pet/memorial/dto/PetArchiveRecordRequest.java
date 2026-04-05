package com.pet.memorial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PetArchiveRecordRequest {

    @NotBlank(message = "档案类型不能为空")
    @Size(max = 30, message = "档案类型长度不能超过30")
    private String archiveType;

    @NotBlank(message = "事项名称不能为空")
    @Size(max = 120, message = "事项名称长度不能超过120")
    private String title;

    @Size(max = 100, message = "数值长度不能超过100")
    private String metricValue;

    @Size(max = 20, message = "单位长度不能超过20")
    private String unit;

    private LocalDate eventDate;

    @Size(max = 1000, message = "备注长度不能超过1000")
    private String note;

    private Boolean reminderEnabled;

    private LocalDateTime reminderAt;

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public LocalDateTime getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(LocalDateTime reminderAt) {
        this.reminderAt = reminderAt;
    }
}
