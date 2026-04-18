package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.dto.request.RegisterApplicantRequest;
import com.example.admissions_management.application.service.AdminApplicantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserWebController {

    private final AdminApplicantService adminApplicantService;

    public UserWebController(AdminApplicantService adminApplicantService) {
        this.adminApplicantService = adminApplicantService;
    }

    @GetMapping
    public String portal(Model model) {
        model.addAttribute("registerApplicantRequest", new RegisterApplicantRequest());
        model.addAttribute("applicants", adminApplicantService.getAllApplicants());
        return "user-portal";
    }

    @PostMapping("/applicants")
    public String register(@ModelAttribute RegisterApplicantRequest registerApplicantRequest) {
        adminApplicantService.registerApplicant(registerApplicantRequest);
        return "redirect:/user";
    }
}
