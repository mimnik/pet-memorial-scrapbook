package com.pet.memorial.repository;

import com.pet.memorial.entity.UserAccountAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountAppealRepository extends JpaRepository<UserAccountAppeal, Long> {

    List<UserAccountAppeal> findByUsernameOrderByCreatedAtDescIdDesc(String username);

    List<UserAccountAppeal> findByStatusOrderByCreatedAtDescIdDesc(String status);

    List<UserAccountAppeal> findAllByOrderByCreatedAtDescIdDesc();
}
