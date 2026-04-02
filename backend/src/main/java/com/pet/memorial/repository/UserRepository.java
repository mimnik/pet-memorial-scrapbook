package com.pet.memorial.repository;

import com.pet.memorial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByUsernameIn(List<String> usernames);

    List<User> findAllByOrderByCreatedAtDesc();

        @Query("""
                select u from User u
                where exists (
                    select p.id from Pet p
                    where p.ownerUsername = u.username
                        and p.isPublic = true
                )
                    and (
                        :keyword is null
                        or lower(u.username) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(u.displayName, '')) like lower(concat('%', :keyword, '%'))
                    )
                order by u.username asc
                """)
        List<User> searchPublicUsersWithPublicPets(@Param("keyword") String keyword);

        long countByAccountFrozenTrue();

        long countByPostingRestrictedTrue();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
