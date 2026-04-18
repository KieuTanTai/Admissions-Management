package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.Applicant;

import java.util.List;
import java.util.Optional;

public interface ApplicantRepository {

    List<Applicant> findAll();

    Optional<Applicant> findByEmail(String email);

    Applicant save(Applicant applicant);
}
