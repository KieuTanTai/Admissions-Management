package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.DiemCongXetTuyenService;
import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.application.service.ScoreManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final ScoreManagementService scoreManagementService;
    private final DiemCongXetTuyenService diemCongXetTuyenService;
    private final NguyenVongXetTuyenService nguyenVongXetTuyenService;

    public AdminWebController(ScoreManagementService scoreManagementService,
            DiemCongXetTuyenService diemCongXetTuyenService,
            NguyenVongXetTuyenService nguyenVongXetTuyenService) {
        this.scoreManagementService = scoreManagementService;
        this.diemCongXetTuyenService = diemCongXetTuyenService;
        this.nguyenVongXetTuyenService = nguyenVongXetTuyenService;
    }

    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("scoreTotal", safeCount(() -> scoreManagementService.getAllRows().size()));
        model.addAttribute("scoreThptTotal",
                safeCount(() -> scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.THPT).size()));
        model.addAttribute("scoreVsatTotal",
                safeCount(() -> scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.VSAT).size()));
        model.addAttribute("scoreDgnlTotal",
                safeCount(() -> scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.DGNL).size()));
        model.addAttribute("diemCongTotal", safeCount(() -> diemCongXetTuyenService.getAll().size()));
        model.addAttribute("nguyenVongTotal", safeCount(() -> nguyenVongXetTuyenService.getAll().size()));
        return "admin-form";
    }

    @GetMapping("/quy-doi")
    public String bangQuyDoi() {
        return "admin/quy-doi-list";
    }

    private int safeCount(java.util.function.IntSupplier supplier) {
        try {
            return supplier.getAsInt();
        } catch (Exception exception) {
            return 0;
        }
    }
}
