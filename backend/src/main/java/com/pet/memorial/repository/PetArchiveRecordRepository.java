package com.pet.memorial.repository;

import com.pet.memorial.entity.PetArchiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PetArchiveRecordRepository extends JpaRepository<PetArchiveRecord, Long> {

    List<PetArchiveRecord> findByPetIdOrderByEventDateDescIdDesc(Long petId);

    List<PetArchiveRecord> findByPetOwnerUsernameAndReminderEnabledTrueAndReminderStatusAndReminderAtLessThanEqualOrderByReminderAtAscIdAsc(
        String ownerUsername,
        String reminderStatus,
        LocalDateTime reminderAt
    );

    void deleteByPetId(Long petId);
}
