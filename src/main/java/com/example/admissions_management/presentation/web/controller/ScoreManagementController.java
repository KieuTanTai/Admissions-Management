package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.ScoreManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.presentation.web.model.ScoreManagementForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/scores")
public class ScoreManagementController {

    private final ScoreManagementService scoreManagementService;

    public ScoreManagementController(ScoreManagementService scoreManagementService) {
        this.scoreManagementService = scoreManagementService;
    }

    @GetMapping
    public String page(@RequestParam(name = "editId", required = false) Integer editId,
                       @RequestParam(name = "statSubject", defaultValue = "TO") String statSubject,
                       Model model) {
        model.addAttribute("thptRows", scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.THPT));
        model.addAttribute("vsatRows", scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.VSAT));
        model.addAttribute("dgnlRows", scoreManagementService.getByType(ScoreManagementService.ScoreMethodType.DGNL));
        model.addAttribute("subjectOptions", scoreManagementService.getSubjectLabels());
        model.addAttribute("statSubject", statSubject);
        model.addAttribute("statRows", scoreManagementService.summarizeByTypeForSubject(statSubject));

        if (!model.containsAttribute("scoreForm")) {
            ScoreManagementForm form = resolveForm(editId);
            model.addAttribute("scoreForm", form);
        }

        return "admin-score-management";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("scoreForm") ScoreManagementForm form,
                       RedirectAttributes redirectAttributes) {
        XtDiemThiXetTuyenEntity saved = scoreManagementService.save(form);
        redirectAttributes.addFlashAttribute("successMessage",
                "Da luu diem thi cho CCCD: " + saved.getCccd() + " (ID: " + saved.getId() + ")");
        return "redirect:/admin/scores";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id) {
        return "redirect:/admin/scores?editId=" + id;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        scoreManagementService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Da xoa ban ghi ID: " + id);
        return "redirect:/admin/scores";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui long chon file Excel (.xlsx/.xls) truoc khi import.");
            return "redirect:/admin/scores";
        }

        ScoreManagementService.ImportResult result = scoreManagementService.importExcel(file);
        if (result.inserted() == 0 && result.updated() == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", result.message());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", result.message());
        }
        return "redirect:/admin/scores";
    }

    private ScoreManagementForm resolveForm(Integer editId) {
        if (editId == null) {
            return scoreManagementService.createEmptyForm();
        }

        return scoreManagementService.findById(editId)
                .map(scoreManagementService::toForm)
                .orElseGet(scoreManagementService::createEmptyForm);
    }
}
