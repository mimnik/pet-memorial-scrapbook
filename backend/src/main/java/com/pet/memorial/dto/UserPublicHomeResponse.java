package com.pet.memorial.dto;

import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.entity.Pet;

import java.util.List;

public class UserPublicHomeResponse {

    private String ownerUsername;
    private List<Pet> publicPets;
    private Pet activePet;
    private List<MemoryEntry> memories;

    public UserPublicHomeResponse() {
    }

    public UserPublicHomeResponse(String ownerUsername, List<Pet> publicPets, Pet activePet, List<MemoryEntry> memories) {
        this.ownerUsername = ownerUsername;
        this.publicPets = publicPets;
        this.activePet = activePet;
        this.memories = memories;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public List<Pet> getPublicPets() {
        return publicPets;
    }

    public void setPublicPets(List<Pet> publicPets) {
        this.publicPets = publicPets;
    }

    public Pet getActivePet() {
        return activePet;
    }

    public void setActivePet(Pet activePet) {
        this.activePet = activePet;
    }

    public List<MemoryEntry> getMemories() {
        return memories;
    }

    public void setMemories(List<MemoryEntry> memories) {
        this.memories = memories;
    }
}
