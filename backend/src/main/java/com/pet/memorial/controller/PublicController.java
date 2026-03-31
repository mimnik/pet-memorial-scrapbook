package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.PublicPetViewResponse;
import com.pet.memorial.dto.UserPublicHomeResponse;
import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.service.MemoryEntryService;
import com.pet.memorial.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PetService petService;
    private final MemoryEntryService memoryEntryService;

    public PublicController(PetService petService, MemoryEntryService memoryEntryService) {
        this.petService = petService;
        this.memoryEntryService = memoryEntryService;
    }

    @GetMapping("/pets/{shareToken}")
    public ApiResponse<PublicPetViewResponse> getPublicPet(@PathVariable String shareToken) {
        Pet pet = petService.getPublicPetByShareToken(shareToken);
        PublicPetViewResponse data = new PublicPetViewResponse(
            pet,
            memoryEntryService.listByPetIdForPublic(pet.getId())
        );
        return ApiResponse.success(data);
    }

    @GetMapping("/users/{ownerUsername}/home")
    public ApiResponse<UserPublicHomeResponse> getUserPublicHome(
        @PathVariable String ownerUsername,
        @RequestParam(required = false) String pet
    ) {
        List<Pet> publicPets = petService.listPublicPetsByOwner(ownerUsername);
        if (publicPets.isEmpty()) {
            return ApiResponse.success(new UserPublicHomeResponse(ownerUsername, List.of(), null, List.of()));
        }

        Pet activePet;
        if (pet == null || pet.trim().isEmpty()) {
            activePet = publicPets.get(0);
        } else {
            activePet = petService.getPublicPetByOwnerAndShareToken(ownerUsername, pet.trim());
        }

        List<MemoryEntry> memories = new ArrayList<>(memoryEntryService.listByPetIdForPublic(activePet.getId()));
        UserPublicHomeResponse response = new UserPublicHomeResponse(ownerUsername, publicPets, activePet, memories);
        return ApiResponse.success(response);
    }
}
