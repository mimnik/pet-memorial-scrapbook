package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.PetRequest;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.service.PetService;
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
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ApiResponse<List<Pet>> listPets() {
        return ApiResponse.success(petService.listPets());
    }

    @GetMapping("/{id}")
    public ApiResponse<Pet> getPet(@PathVariable Long id) {
        return ApiResponse.success(petService.getPet(id));
    }

    @PostMapping
    public ApiResponse<Pet> createPet(@Valid @RequestBody PetRequest request) {
        return ApiResponse.success("创建成功", petService.createPet(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Pet> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequest request) {
        return ApiResponse.success("更新成功", petService.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ApiResponse.success("删除成功", null);
    }
}
