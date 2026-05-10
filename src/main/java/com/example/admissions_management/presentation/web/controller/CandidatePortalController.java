package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.CandidatePortalService;
import com.example.admissions_management.application.service.candidate.OptionItem;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import com.example.admissions_management.presentation.web.model.DgnlCalculatorForm;
import com.example.admissions_management.presentation.web.model.VsatThptCalculatorForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/candidate")
public class CandidatePortalController {

    private final CandidatePortalService candidatePortalService;

    public CandidatePortalController(CandidatePortalService candidatePortalService) {
        this.candidatePortalService = candidatePortalService;
    }

    @ModelAttribute("majorOptions")
    public List<OptionItem> majorOptions() {
        return candidatePortalService.getMajorOptions();
    }

    @ModelAttribute("priorityObjectOptions")
    public List<OptionItem> priorityObjectOptions() {
        return candidatePortalService.getPriorityObjectOptions();
    }

    @ModelAttribute("priorityRegionOptions")
    public List<OptionItem> priorityRegionOptions() {
        return candidatePortalService.getPriorityRegionOptions();
    }

    @ModelAttribute("bonusSubjectOptions")
    public List<OptionItem> bonusSubjectOptions() {
        return candidatePortalService.getBonusSubjectOptions();
    }

    @GetMapping
    public String portal(Model model) {
        ensureDefaultForms(model);
        model.addAttribute("activeTab", "lookup");
        return "candidate-portal";
    }

    @PostMapping("/lookup")
    public String lookup(@ModelAttribute("lookupForm") CandidateLookupForm lookupForm, Model model) {
        ensureDefaultForms(model);
        model.addAttribute("lookupForm", lookupForm);
        model.addAttribute("lookupResult", candidatePortalService.lookupResult(lookupForm));
        model.addAttribute("activeTab", "lookup");
        return "candidate-portal";
    }

    @PostMapping("/calculate/dgnl")
    public String calculateDgnl(@ModelAttribute("dgnlForm") DgnlCalculatorForm dgnlForm, Model model) {
        ensureDefaultForms(model);
        model.addAttribute("dgnlForm", dgnlForm);
        model.addAttribute("dgnlResult", candidatePortalService.calculateDgnl(dgnlForm));
        model.addAttribute("activeTab", "dgnl");
        return "candidate-portal";
    }

    @PostMapping("/calculate/vsat-thpt")
    public String calculateVsatThpt(@ModelAttribute("vsatThptForm") VsatThptCalculatorForm vsatThptForm,
            Model model) {
        ensureDefaultForms(model);
        model.addAttribute("vsatThptForm", vsatThptForm);
        model.addAttribute("vsatThptResult", candidatePortalService.calculateVsatThpt(vsatThptForm));
        model.addAttribute("activeTab", "vsat-thpt");
        return "candidate-portal";
    }

    private void ensureDefaultForms(Model model) {
        if (!model.containsAttribute("lookupForm")) {
            model.addAttribute("lookupForm", new CandidateLookupForm());
        }

        if (!model.containsAttribute("dgnlForm")) {
            DgnlCalculatorForm dgnlForm = new DgnlCalculatorForm();
            dgnlForm.setPriorityObjectCode("NONE");
            dgnlForm.setPriorityRegionCode("KV3");
            model.addAttribute("dgnlForm", dgnlForm);
        }

        if (!model.containsAttribute("vsatThptForm")) {
            VsatThptCalculatorForm vsatThptForm = new VsatThptCalculatorForm();
            vsatThptForm.setMethodType("VSAT");
            vsatThptForm.setPriorityObjectCode("NONE");
            vsatThptForm.setPriorityRegionCode("KV3");
            vsatThptForm.setBonusSubjectCode("NONE");
            model.addAttribute("vsatThptForm", vsatThptForm);
        }
    }
}