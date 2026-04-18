package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.dto.request.RegisterApplicantRequest;
import com.example.admissions_management.application.dto.response.ApplicantResponse;
import com.example.admissions_management.application.service.AdminApplicantService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminConsoleController {

    private final AdminApplicantService adminApplicantService;

    public AdminConsoleController(AdminApplicantService adminApplicantService) {
        this.adminApplicantService = adminApplicantService;
    }

    public List<ApplicantResponse> loadApplicants() {
        return adminApplicantService.getAllApplicants();
    }

    public ApplicantResponse registerApplicant(String fullName, String email, String program) {
        return adminApplicantService.registerApplicant(new RegisterApplicantRequest(fullName, email, program));
    }
}
