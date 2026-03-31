package com.pet.memorial.dto;

import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.entity.Pet;

import java.util.List;

public class PublicPetViewResponse {

    private Pet pet;
    private List<MemoryEntry> memories;

    public PublicPetViewResponse() {
    }

    public PublicPetViewResponse(Pet pet, List<MemoryEntry> memories) {
        this.pet = pet;
        this.memories = memories;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public List<MemoryEntry> getMemories() {
        return memories;
    }

    public void setMemories(List<MemoryEntry> memories) {
        this.memories = memories;
    }
}
