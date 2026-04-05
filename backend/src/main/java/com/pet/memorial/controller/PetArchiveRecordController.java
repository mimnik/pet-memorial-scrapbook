package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.PetArchiveRecordRequest;
import com.pet.memorial.dto.PetArchiveReminderSnoozeRequest;
import com.pet.memorial.entity.PetArchiveRecord;
import com.pet.memorial.service.PetArchiveRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PetArchiveRecordController {

    private final PetArchiveRecordService petArchiveRecordService;

    public PetArchiveRecordController(PetArchiveRecordService petArchiveRecordService) {
        this.petArchiveRecordService = petArchiveRecordService;
    }

    @GetMapping("/pets/{petId}/archives")
    public ApiResponse<List<PetArchiveRecord>> listByPetId(@PathVariable Long petId) {
        return ApiResponse.success(petArchiveRecordService.listByPetId(petId));
    }

    @PostMapping("/pets/{petId}/archives")
    public ApiResponse<PetArchiveRecord> create(
        @PathVariable Long petId,
        @Valid @RequestBody PetArchiveRecordRequest request
    ) {
        return ApiResponse.success("创建成功", petArchiveRecordService.create(petId, request));
    }

    @PutMapping("/pet-archives/{id}")
    public ApiResponse<PetArchiveRecord> update(
        @PathVariable Long id,
        @Valid @RequestBody PetArchiveRecordRequest request
    ) {
        return ApiResponse.success("更新成功", petArchiveRecordService.update(id, request));
    }

    @DeleteMapping("/pet-archives/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        petArchiveRecordService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/pet-archives/reminders/due")
    public ApiResponse<List<PetArchiveRecord>> listDueReminders() {
        return ApiResponse.success(petArchiveRecordService.listDueReminders());
    }

    @PostMapping("/pet-archives/{id}/reminder/complete")
    public ApiResponse<PetArchiveRecord> completeReminder(@PathVariable Long id) {
        return ApiResponse.success("提醒已完成", petArchiveRecordService.completeReminder(id));
    }

    @PostMapping("/pet-archives/{id}/reminder/snooze")
    public ApiResponse<PetArchiveRecord> snoozeReminder(
        @PathVariable Long id,
        @Valid @RequestBody PetArchiveReminderSnoozeRequest request
    ) {
        return ApiResponse.success(
            "已设置稍后提醒",
            petArchiveRecordService.snoozeReminder(id, request.getDelayHours())
        );
    }
}
