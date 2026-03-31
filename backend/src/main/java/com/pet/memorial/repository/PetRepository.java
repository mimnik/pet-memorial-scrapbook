package com.pet.memorial.repository;

import com.pet.memorial.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

	List<Pet> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

	Optional<Pet> findByIdAndOwnerUsername(Long id, String ownerUsername);

	Optional<Pet> findByShareTokenAndIsPublicTrue(String shareToken);

	List<Pet> findByOwnerUsernameAndIsPublicTrueOrderByCreatedAtDesc(String ownerUsername);

	int countByOwnerUsernameAndIsPublicTrue(String ownerUsername);
}
