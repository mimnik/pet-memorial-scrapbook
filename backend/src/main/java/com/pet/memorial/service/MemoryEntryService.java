package com.pet.memorial.service;

import com.pet.memorial.dto.MemoryEntryRequest;
import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.MemoryEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class MemoryEntryService {

    private final MemoryEntryRepository memoryEntryRepository;
    private final PetService petService;

    public MemoryEntryService(MemoryEntryRepository memoryEntryRepository, PetService petService) {
        this.memoryEntryRepository = memoryEntryRepository;
        this.petService = petService;
    }

    public List<MemoryEntry> listByPetId(Long petId) {
        Long safePetId = Objects.requireNonNull(petId, "petId不能为空");
        petService.getPet(safePetId);
        return memoryEntryRepository.findByPetIdOrderByEventDateDescIdDesc(safePetId);
    }

    public List<MemoryEntry> listByPetIdForPublic(Long petId) {
        Long safePetId = Objects.requireNonNull(petId, "petId不能为空");
        return memoryEntryRepository.findByPetIdOrderByEventDateDescIdDesc(safePetId);
    }

    public MemoryEntry getById(Long id) {
        Long safeId = Objects.requireNonNull(id, "id不能为空");
        return memoryEntryRepository.findById(safeId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到回忆条目，id=" + id));
    }

    public MemoryEntry create(Long petId, MemoryEntryRequest request) {
        Pet pet = petService.getPet(petId);
        MemoryEntry memoryEntry = new MemoryEntry();
        memoryEntry.setPet(pet);
        applyRequest(memoryEntry, request);
        if (memoryEntry.getEventDate() == null) {
            memoryEntry.setEventDate(LocalDate.now());
        }
        return memoryEntryRepository.save(Objects.requireNonNull(memoryEntry));
    }

    public MemoryEntry update(Long id, MemoryEntryRequest request) {
        MemoryEntry memoryEntry = getById(id);
        applyRequest(memoryEntry, request);
        return memoryEntryRepository.save(Objects.requireNonNull(memoryEntry));
    }

    public void delete(Long id) {
        MemoryEntry memoryEntry = getById(id);
        memoryEntryRepository.delete(Objects.requireNonNull(memoryEntry));
    }

    private void applyRequest(MemoryEntry memoryEntry, MemoryEntryRequest request) {
        memoryEntry.setTitle(request.getTitle());
        memoryEntry.setContent(request.getContent());
        memoryEntry.setEventDate(request.getEventDate());
        memoryEntry.setLocation(request.getLocation());
        memoryEntry.setImageUrl(request.getImageUrl());
        memoryEntry.setVideoUrl(request.getVideoUrl());
        memoryEntry.setVideoCoverUrl(request.getVideoCoverUrl());
        memoryEntry.setVideoDurationSeconds(request.getVideoDurationSeconds());
    }
}
