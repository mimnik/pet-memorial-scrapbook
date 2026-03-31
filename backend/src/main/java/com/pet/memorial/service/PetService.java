package com.pet.memorial.service;

import com.pet.memorial.dto.PetRequest;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.exception.ResourceNotFoundException;
import com.pet.memorial.repository.MemoryEntryRepository;
import com.pet.memorial.repository.PetRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final MemoryEntryRepository memoryEntryRepository;

    public PetService(PetRepository petRepository, MemoryEntryRepository memoryEntryRepository) {
        this.petRepository = petRepository;
        this.memoryEntryRepository = memoryEntryRepository;
    }

    public List<Pet> listPets() {
        return petRepository.findByOwnerUsernameOrderByCreatedAtDesc(getCurrentUsername());
    }

    public Pet getPet(Long id) {
        Long safeId = Objects.requireNonNull(id, "id不能为空");
        return petRepository.findByIdAndOwnerUsername(safeId, getCurrentUsername())
            .orElseThrow(() -> new ResourceNotFoundException("未找到宠物，id=" + id));
    }

    public Pet createPet(PetRequest request) {
        Pet pet = new Pet();
        pet.setOwnerUsername(getCurrentUsername());
        pet.setShareToken(generateShareToken());
        applyRequest(pet, request);
        return petRepository.save(Objects.requireNonNull(pet));
    }

    public Pet updatePet(Long id, PetRequest request) {
        Pet pet = getPet(id);
        applyRequest(pet, request);
        return petRepository.save(Objects.requireNonNull(pet));
    }

    public void deletePet(Long id) {
        Pet pet = getPet(id);
        Long safeId = Objects.requireNonNull(id, "id不能为空");
        memoryEntryRepository.deleteByPetId(safeId);
        petRepository.delete(Objects.requireNonNull(pet));
    }

    private void applyRequest(Pet pet, PetRequest request) {
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setBirthDate(request.getBirthDate());
        pet.setMemorialDate(request.getMemorialDate());
        pet.setAvatarUrl(request.getAvatarUrl());
        pet.setDescription(request.getDescription());
        pet.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
    }

    public Pet getPublicPetByShareToken(String shareToken) {
        return petRepository.findByShareTokenAndIsPublicTrue(shareToken)
            .orElseThrow(() -> new ResourceNotFoundException("公开纪念页不存在或未开放"));
    }

    public List<Pet> listPublicPetsByOwner(String ownerUsername) {
        return petRepository.findByOwnerUsernameAndIsPublicTrueOrderByCreatedAtDesc(ownerUsername);
    }

    public Pet getPublicPetByOwnerAndShareToken(String ownerUsername, String shareToken) {
        Pet pet = getPublicPetByShareToken(shareToken);
        if (!pet.getOwnerUsername().equals(ownerUsername)) {
            throw new ResourceNotFoundException("该公开宠物不属于当前主页用户");
        }
        return pet;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("当前用户未认证");
        }
        return authentication.getName();
    }

    private String generateShareToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
