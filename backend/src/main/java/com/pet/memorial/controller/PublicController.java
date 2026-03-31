package com.pet.memorial.controller;

import com.pet.memorial.common.ApiResponse;
import com.pet.memorial.dto.PublicCommunityPostResponse;
import com.pet.memorial.dto.PublicPetViewResponse;
import com.pet.memorial.dto.PublicUserSearchItemResponse;
import com.pet.memorial.dto.UserPublicHomeResponse;
import com.pet.memorial.entity.CommunityPost;
import com.pet.memorial.entity.MemoryEntry;
import com.pet.memorial.entity.Pet;
import com.pet.memorial.entity.User;
import com.pet.memorial.repository.CommunityPostRepository;
import com.pet.memorial.repository.PetRepository;
import com.pet.memorial.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final CommunityPostRepository communityPostRepository;

    public PublicController(
        PetService petService,
        MemoryEntryService memoryEntryService,
        UserRepository userRepository,
        PetRepository petRepository,
        CommunityPostRepository communityPostRepository
    ) {
        this.petService = petService;
        this.memoryEntryService = memoryEntryService;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.communityPostRepository = communityPostRepository;
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

    @GetMapping("/users/search")
    public ApiResponse<List<PublicUserSearchItemResponse>> searchPublicUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "12") int limit
    ) {
        String keywordNormalized = trimToNull(keyword);
        int safeLimit = normalizeLimit(limit, 12, 30);

        List<PublicUserSearchItemResponse> users = userRepository.searchPublicUsersWithPublicPets(keywordNormalized).stream()
            .limit(safeLimit)
            .map(this::toPublicUserSearchItem)
            .toList();

        return ApiResponse.success(users);
    }

    @GetMapping("/users/{ownerUsername}/home")
    public ApiResponse<UserPublicHomeResponse> getUserPublicHome(
        @PathVariable String ownerUsername,
        @RequestParam(required = false) String pet
    ) {
        List<Pet> publicPets = petService.listPublicPetsByOwner(ownerUsername);
        if (publicPets.isEmpty()) {
            return ApiResponse.success(new UserPublicHomeResponse(ownerUsername, List.of(), null, List.of(), List.of()));
        }

        Pet activePet;
        if (pet == null || pet.trim().isEmpty()) {
            activePet = publicPets.get(0);
        } else {
            activePet = petService.getPublicPetByOwnerAndShareToken(ownerUsername, pet.trim());
        }

        List<MemoryEntry> memories = new ArrayList<>(memoryEntryService.listByPetIdForPublic(activePet.getId()));
        List<PublicCommunityPostResponse> communityPosts = communityPostRepository
            .findByAuthorUsernameOrderByCreatedAtDescIdDesc(ownerUsername)
            .stream()
            .limit(30)
            .map(this::toPublicCommunityPost)
            .toList();

        UserPublicHomeResponse response = new UserPublicHomeResponse(
            ownerUsername,
            publicPets,
            activePet,
            memories,
            communityPosts
        );
        return ApiResponse.success(response);
    }

    private PublicUserSearchItemResponse toPublicUserSearchItem(User user) {
        PublicUserSearchItemResponse row = new PublicUserSearchItemResponse();
        row.setUsername(user.getUsername());
        row.setDisplayName(user.getDisplayName());
        row.setAvatarUrl(user.getAvatarUrl());
        row.setBio(user.getBio());
        row.setPublicPetCount(petRepository.countByOwnerUsernameAndIsPublicTrue(user.getUsername()));
        row.setRecentPostCount(communityPostRepository.countByAuthorUsername(user.getUsername()));
        return row;
    }

    private PublicCommunityPostResponse toPublicCommunityPost(CommunityPost post) {
        PublicCommunityPostResponse item = new PublicCommunityPostResponse();
        item.setId(post.getId());
        item.setPetId(post.getPet().getId());
        item.setPetName(post.getPet().getName());
        item.setTopicName(post.getTopic() == null ? null : post.getTopic().getName());
        item.setTitle(post.getTitle());
        item.setContent(post.getContent());
        item.setImageUrl(post.getImageUrl());
        item.setVideoUrl(post.getVideoUrl());
        item.setVideoCoverUrl(post.getVideoCoverUrl());
        item.setVideoDurationSeconds(post.getVideoDurationSeconds());
        item.setMoodTag(post.getMoodTag());
        item.setNarrativeMode(post.getNarrativeMode());
        item.setPetVoice(post.getPetVoice());
        item.setRelayEnabled(post.getRelayEnabled());
        item.setLikeCount(post.getLikeCount());
        item.setCommentCount(post.getCommentCount());
        item.setCreatedAt(post.getCreatedAt());
        return item;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int normalizeLimit(int limit, int fallback, int max) {
        if (limit <= 0) {
            return fallback;
        }
        return Math.min(limit, max);
    }
}
