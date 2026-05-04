package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.CandidateManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.presentation.web.model.CandidateForm;
import com.example.admissions_management.presentation.web.model.CandidateImportResult;
import org.springframework.data.domain.Page;
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
@RequestMapping("/admin/candidates")
public class CandidateManagementController {

    private final CandidateManagementService candidateManagementService;

    public CandidateManagementController(CandidateManagementService candidateManagementService) {
        this.candidateManagementService = candidateManagementService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<XtThiSinhXetTuyen25Entity> candidatePage = candidateManagementService.searchCandidates(q, page);
        model.addAttribute("candidates", candidatePage);
        model.addAttribute("query", q != null ? q.trim() : "");
        return "candidates";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("candidateForm", new CandidateForm());
        model.addAttribute("pageTitle", "Tao moi thi sinh");
        return "candidate-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        return candidateManagementService.findById(id)
                .map(entity -> {
                    CandidateForm form = toForm(entity);
                    model.addAttribute("candidateForm", form);
                    model.addAttribute("pageTitle", "Chinh sua thi sinh");
                    return "candidate-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay thi sinh voi id=" + id);
                    return "redirect:/admin/candidates";
                });
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CandidateForm candidateForm, RedirectAttributes redirectAttributes) {
        candidateManagementService.saveCandidate(candidateForm);
        redirectAttributes.addFlashAttribute("successMessage", "Luu thong tin thi sinh thanh cong.");
        return "redirect:/admin/candidates";
    }

    @PostMapping("/import")
    public String importCandidates(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        CandidateImportResult result = candidateManagementService.importCandidates(file);
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", String.join("; ", result.getErrors()));
        }
        redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        return "redirect:/admin/candidates";
    }

    @PostMapping("/{id}/delete")
    public String deleteCandidate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        candidateManagementService.deleteCandidate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xoa thi sinh thanh cong.");
        return "redirect:/admin/candidates";
    }

    @PostMapping("/delete-all")
    public String deleteAllCandidates(RedirectAttributes redirectAttributes) {
        candidateManagementService.deleteAllCandidates();
        redirectAttributes.addFlashAttribute("successMessage", "Da xoa tat ca thi sinh.");
        return "redirect:/admin/candidates";
    }

    private CandidateForm toForm(XtThiSinhXetTuyen25Entity entity) {
        CandidateForm form = new CandidateForm();
        form.setId(entity.getId());
        form.setCccd(entity.getCccd());
        form.setSoBaoDanh(entity.getSoBaoDanh());
        form.setHo(entity.getHo());
        form.setTen(entity.getTen());
        form.setNgaySinh(entity.getNgaySinh());
        form.setDienThoai(entity.getDienThoai());
        form.setGioiTinh(entity.getGioiTinh());
        form.setEmail(entity.getEmail());
        form.setNoiSinh(entity.getNoiSinh());
        form.setDoiTuong(entity.getDoiTuong());
        form.setKhuVuc(entity.getKhuVuc());
        form.setPassword(entity.getPassword());
        return form;
    }
}
