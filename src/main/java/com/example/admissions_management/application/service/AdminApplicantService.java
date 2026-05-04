package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.RegisterApplicantRequest;
import com.example.admissions_management.application.dto.response.ApplicantResponse;
import com.example.admissions_management.domain.model.Applicant;
import com.example.admissions_management.domain.repository.IApplicantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminApplicantService {

    private final IApplicantRepository applicantRepository;

    public AdminApplicantService(IApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    public List<ApplicantResponse> getAllApplicants() {
        return applicantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicantResponse registerApplicant(RegisterApplicantRequest request) {
        applicantRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("Applicant email already exists: " + request.getEmail());
        });

        Applicant saved = applicantRepository.save(new Applicant(
                null,
                request.getFullName(),
                request.getEmail(),
                request.getProgram()
        ));

        return toResponse(saved);
    }

    private ApplicantResponse toResponse(Applicant applicant) {
        return new ApplicantResponse(
                applicant.getId(),
                applicant.getFullName(),
                applicant.getEmail(),
                applicant.getProgram()
        );
    }
}
