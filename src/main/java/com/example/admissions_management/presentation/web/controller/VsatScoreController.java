package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.dto.request.ScoreCalculationRequest;
import com.example.admissions_management.application.dto.response.ScoreResultResponse;
import com.example.admissions_management.application.service.VsatScoreService;
import com.example.admissions_management.application.service.candidate.OptionItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tra-cuu-diem")
public class VsatScoreController {

    private final VsatScoreService vsatScoreService;

    public VsatScoreController(VsatScoreService vsatScoreService) {
        this.vsatScoreService = vsatScoreService;
    }

    @ModelAttribute("majorOptions")
    public List<OptionItem> majorOptions() {
        return vsatScoreService.getMajorOptions();
    }

    @ModelAttribute("priorityObjectOptions")
    public List<OptionItem> priorityObjectOptions() {
        return vsatScoreService.getPriorityObjectOptions();
    }

    @ModelAttribute("priorityRegionOptions")
    public List<OptionItem> priorityRegionOptions() {
        return vsatScoreService.getPriorityRegionOptions();
    }

    @ModelAttribute("bonusSubjectOptions")
    public List<OptionItem> bonusSubjectOptions() {
        return vsatScoreService.getBonusSubjectOptions();
    }

    @ModelAttribute("convertibleCodes")
    public List<String> convertibleCodes() {
        return vsatScoreService.getConvertibleSubjectCodes();
    }

    @ModelAttribute("scoreCalculationRequest")
    public ScoreCalculationRequest scoreCalculationRequest() {
        ScoreCalculationRequest request = new ScoreCalculationRequest();
        request.setLoaiDiem("VSAT");
        request.setKhuVuc("KV3");
        request.setDoiTuong("NONE");
        request.setMonCongDiem("NONE");
        request.setDiemCong(0.0d);
        return request;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("activePage", "tra-cuu-diem");
        return "tra-cuu-diem";
    }

    @PostMapping
    public String calculate(@ModelAttribute("scoreCalculationRequest") ScoreCalculationRequest request, Model model) {
        ScoreResultResponse result = vsatScoreService.calculate(request);
        model.addAttribute("scoreResultResponse", result);
        model.addAttribute("activePage", "tra-cuu-diem");
        return "tra-cuu-diem";
    }
}