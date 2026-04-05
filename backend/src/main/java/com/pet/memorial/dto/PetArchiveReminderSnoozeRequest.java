package com.pet.memorial.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PetArchiveReminderSnoozeRequest {

    @NotNull(message = "稍后小时数不能为空")
    @Min(value = 1, message = "稍后小时数必须大于0")
    @Max(value = 720, message = "稍后小时数不能超过720")
    private Integer delayHours;

    public Integer getDelayHours() {
        return delayHours;
    }

    public void setDelayHours(Integer delayHours) {
        this.delayHours = delayHours;
    }
}
