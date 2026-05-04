package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.ApplicantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ISpringDataApplicantRepository extends JpaRepository<ApplicantEntity, Long> {

    Optional<ApplicantEntity> findByEmail(String email);
}
