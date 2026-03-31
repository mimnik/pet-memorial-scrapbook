package com.pet.memorial.repository;

import com.pet.memorial.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    List<ContentReport> findByReporterUsernameOrderByCreatedAtDescIdDesc(String reporterUsername);

    List<ContentReport> findByStatusOrderByCreatedAtDescIdDesc(String status);

    List<ContentReport> findAllByOrderByCreatedAtDescIdDesc();
}
