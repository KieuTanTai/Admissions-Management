package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.ScoreManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.presentation.web.model.ScoreManagementForm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class ScoreManagementConsoleController {

    private final ScoreManagementService scoreManagementService;

    public ScoreManagementConsoleController(ScoreManagementService scoreManagementService) {
        this.scoreManagementService = scoreManagementService;
    }

    public List<XtDiemThiXetTuyenEntity> loadAll() {
        return scoreManagementService.getAllRows();
    }

    public List<XtDiemThiXetTuyenEntity> loadByType(String methodType) {
        if (methodType == null || methodType.isBlank() || "ALL".equalsIgnoreCase(methodType)) {
            return loadAll();
        }

        ScoreManagementService.ScoreMethodType type = switch (methodType.toUpperCase()) {
            case "VSAT" -> ScoreManagementService.ScoreMethodType.VSAT;
            case "DGNL" -> ScoreManagementService.ScoreMethodType.DGNL;
            default -> ScoreManagementService.ScoreMethodType.THPT;
        };

        return scoreManagementService.getByType(type);
    }

    public XtDiemThiXetTuyenEntity save(ScoreManagementForm form) {
        return scoreManagementService.save(form);
    }

    public Optional<XtDiemThiXetTuyenEntity> findById(Integer id) {
        return scoreManagementService.findById(id);
    }

    public void deleteById(Integer id) {
        scoreManagementService.delete(id);
    }

    public ScoreManagementForm toForm(XtDiemThiXetTuyenEntity entity) {
        return scoreManagementService.toForm(entity);
    }

    public BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return new BigDecimal(value.trim());
    }

    public ScoreManagementService.ImportResult importExcel(java.io.File file) {
        return scoreManagementService.importExcel(file);
    }
}
