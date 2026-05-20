package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.CandidatePortalService;
import com.example.admissions_management.application.service.candidate.CandidateLookupResult;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/candidate")
public class CandidatePortalController {

    private final CandidatePortalService candidatePortalService;

    public CandidatePortalController(CandidatePortalService candidatePortalService) {
        this.candidatePortalService = candidatePortalService;
    }

    // Hiển thị form đăng nhập
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("lookupForm")) {
            model.addAttribute("lookupForm", new CandidateLookupForm());
        }
        return "candidate-login"; // template đăng nhập
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String login(@ModelAttribute("lookupForm") CandidateLookupForm form, Model model) {
        CandidateLookupResult result = candidatePortalService.lookupResult(form);

        if (!result.isFound()) {
            // Login thất bại → quay lại form với thông báo
            model.addAttribute("lookupResult", result);
            return "candidate-login";
        }

        // Login thành công → chuyển sang trang kết quả
        model.addAttribute("lookupResult", result);
        return "candidate-result"; // template hiển thị kết quả
    }
}