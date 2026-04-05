package com.pet.memorial.service;

import com.pet.memorial.dto.PetArchiveRecordRequest;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.entity.PetArchiveRecord;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.PetArchiveRecordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class PetArchiveRecordService {

    private static final String REMINDER_STATUS_PENDING = "PENDING";
    private static final String REMINDER_STATUS_COMPLETED = "COMPLETED";

    private static final Set<String> SUPPORTED_ARCHIVE_TYPES = Set.of(
        "VACCINE",
        "CHECKUP",
        "DEWORMING",
        "MEDICATION",
        "OTHER"
    );

    private final PetArchiveRecordRepository petArchiveRecordRepository;
    private final PetService petService;

    public PetArchiveRecordService(PetArchiveRecordRepository petArchiveRecordRepository, PetService petService) {
        this.petArchiveRecordRepository = petArchiveRecordRepository;
        this.petService = petService;
    }

    public List<PetArchiveRecord> listByPetId(Long petId) {
        Long safePetId = Objects.requireNonNull(petId, "petId不能为空");
        petService.getPet(safePetId);
        return petArchiveRecordRepository.findByPetIdOrderByEventDateDescIdDesc(safePetId);
    }

    public PetArchiveRecord create(Long petId, PetArchiveRecordRequest request) {
        Long safePetId = Objects.requireNonNull(petId, "petId不能为空");
        Pet pet = petService.getPet(safePetId);

        PetArchiveRecord record = new PetArchiveRecord();
        record.setPet(pet);
        applyRequest(record, request);
        if (record.getEventDate() == null) {
            record.setEventDate(LocalDate.now());
        }
        return petArchiveRecordRepository.save(Objects.requireNonNull(record));
    }

    public PetArchiveRecord update(Long id, PetArchiveRecordRequest request) {
        PetArchiveRecord record = getOwnedRecord(id);
        applyRequest(record, request);
        return petArchiveRecordRepository.save(Objects.requireNonNull(record));
    }

    public void delete(Long id) {
        PetArchiveRecord record = getOwnedRecord(id);
        petArchiveRecordRepository.delete(Objects.requireNonNull(record));
    }

    public List<PetArchiveRecord> listDueReminders() {
        return petArchiveRecordRepository
            .findByPetOwnerUsernameAndReminderEnabledTrueAndReminderStatusAndReminderAtLessThanEqualOrderByReminderAtAscIdAsc(
                getCurrentUsername(),
                REMINDER_STATUS_PENDING,
                LocalDateTime.now()
            );
    }

    public PetArchiveRecord completeReminder(Long id) {
        PetArchiveRecord record = getOwnedRecord(id);
        ensureReminderEnabled(record);
        record.setReminderStatus(REMINDER_STATUS_COMPLETED);
        record.setReminderCompletedAt(LocalDateTime.now());
        return petArchiveRecordRepository.save(Objects.requireNonNull(record));
    }

    public PetArchiveRecord snoozeReminder(Long id, int delayHours) {
        if (delayHours <= 0) {
            throw new IllegalArgumentException("稍后时间必须大于0小时");
        }

        PetArchiveRecord record = getOwnedRecord(id);
        ensureReminderEnabled(record);
        record.setReminderStatus(REMINDER_STATUS_PENDING);
        record.setReminderAt(LocalDateTime.now().plusHours(delayHours));
        record.setReminderCompletedAt(null);
        return petArchiveRecordRepository.save(Objects.requireNonNull(record));
    }

    private void applyRequest(PetArchiveRecord record, PetArchiveRecordRequest request) {
        String archiveType = normalizeArchiveType(request.getArchiveType());
        if (!SUPPORTED_ARCHIVE_TYPES.contains(archiveType)) {
            throw new IllegalArgumentException("不支持的档案类型");
        }

        String title = trimToNull(request.getTitle());
        if (title == null) {
            throw new IllegalArgumentException("事项名称不能为空");
        }

        record.setArchiveType(archiveType);
        record.setTitle(title);
        record.setMetricValue(trimToNull(request.getMetricValue()));
        record.setUnit(trimToNull(request.getUnit()));
        record.setEventDate(request.getEventDate());
        record.setNote(trimToNull(request.getNote()));

        boolean reminderEnabled = Boolean.TRUE.equals(request.getReminderEnabled());
        record.setReminderEnabled(reminderEnabled);

        if (!reminderEnabled) {
            record.setReminderAt(null);
            record.setReminderStatus(REMINDER_STATUS_PENDING);
            record.setReminderCompletedAt(null);
            return;
        }

        LocalDateTime reminderAt = request.getReminderAt();
        if (reminderAt == null) {
            throw new IllegalArgumentException("开启提醒后请设置提醒时间");
        }

        record.setReminderAt(reminderAt);
        record.setReminderStatus(REMINDER_STATUS_PENDING);
        record.setReminderCompletedAt(null);
    }

    private PetArchiveRecord getOwnedRecord(Long id) {
        Long safeId = Objects.requireNonNull(id, "id不能为空");
        PetArchiveRecord record = petArchiveRecordRepository.findById(safeId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到宠物档案记录，id=" + id));

        Long petId = record.getPet() == null ? null : record.getPet().getId();
        petService.getPet(petId);
        return record;
    }

    private void ensureReminderEnabled(PetArchiveRecord record) {
        if (!Boolean.TRUE.equals(record.getReminderEnabled()) || record.getReminderAt() == null) {
            throw new IllegalArgumentException("该档案未开启提醒");
        }
    }

    private String normalizeArchiveType(String value) {
        String safe = trimToNull(value);
        if (safe == null) {
            throw new IllegalArgumentException("档案类型不能为空");
        }
        return safe.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }
}
