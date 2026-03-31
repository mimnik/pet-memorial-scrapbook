package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.MemoryEntryRequest;
import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.service.MemoryEntryService;
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
public class MemoryEntryController {

    private final MemoryEntryService memoryEntryService;

    public MemoryEntryController(MemoryEntryService memoryEntryService) {
        this.memoryEntryService = memoryEntryService;
    }

    @GetMapping("/pets/{petId}/memories")
    public ApiResponse<List<MemoryEntry>> listByPetId(@PathVariable Long petId) {
        return ApiResponse.success(memoryEntryService.listByPetId(petId));
    }

    @GetMapping("/memories/{id}")
    public ApiResponse<MemoryEntry> getById(@PathVariable Long id) {
        return ApiResponse.success(memoryEntryService.getById(id));
    }

    @PostMapping("/pets/{petId}/memories")
    public ApiResponse<MemoryEntry> create(@PathVariable Long petId, @Valid @RequestBody MemoryEntryRequest request) {
        return ApiResponse.success("创建成功", memoryEntryService.create(petId, request));
    }

    @PutMapping("/memories/{id}")
    public ApiResponse<MemoryEntry> update(@PathVariable Long id, @Valid @RequestBody MemoryEntryRequest request) {
        return ApiResponse.success("更新成功", memoryEntryService.update(id, request));
    }

    @DeleteMapping("/memories/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memoryEntryService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
