package com.pet.memorial.repository;

import com.pet.memorial.entity.MemoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryEntryRepository extends JpaRepository<MemoryEntry, Long> {

    List<MemoryEntry> findByPetIdOrderByEventDateDescIdDesc(Long petId);

    void deleteByPetId(Long petId);
}
