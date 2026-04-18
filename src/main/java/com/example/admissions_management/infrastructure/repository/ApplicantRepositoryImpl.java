package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.Applicant;
import com.example.admissions_management.domain.repository.ApplicantRepository;
import com.example.admissions_management.infrastructure.persistence.entity.ApplicantEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataApplicantRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ApplicantRepositoryImpl implements ApplicantRepository {

    private final SpringDataApplicantRepository springDataApplicantRepository;

    public ApplicantRepositoryImpl(SpringDataApplicantRepository springDataApplicantRepository) {
        this.springDataApplicantRepository = springDataApplicantRepository;
    }

    @Override
    public List<Applicant> findAll() {
        return springDataApplicantRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Applicant> findByEmail(String email) {
        return springDataApplicantRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Applicant save(Applicant applicant) {
        ApplicantEntity entity = new ApplicantEntity();
        entity.setId(applicant.getId());
        entity.setFullName(applicant.getFullName());
        entity.setEmail(applicant.getEmail());
        entity.setProgram(applicant.getProgram());

        ApplicantEntity saved = springDataApplicantRepository.save(entity);
        return toDomain(saved);
    }

    private Applicant toDomain(ApplicantEntity entity) {
        return new Applicant(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getProgram()
        );
    }
}
