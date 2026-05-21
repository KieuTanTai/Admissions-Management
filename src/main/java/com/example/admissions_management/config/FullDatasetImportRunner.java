package com.example.admissions_management.config;

import com.example.admissions_management.application.service.CandidateManagementService;
import com.example.admissions_management.application.service.MajorManagementService;
import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.application.service.ScoreManagementService;
import com.example.admissions_management.application.service.ToHopMonThiManagementService;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemThiXetTuyenRepository;
import com.example.admissions_management.presentation.form.controller.BangQuyDoiConsoleController;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app", name = "importAllData", havingValue = "true")
public class FullDatasetImportRunner implements CommandLineRunner {

    private static final String BASE = "src/main/java/com/example/admissions_management/shared/docs/excels";

    private final CandidateManagementService candidateManagementService;
    private final ScoreManagementService scoreManagementService;
    private final ToHopMonThiManagementService toHopMonThiManagementService;
    private final MajorManagementService majorManagementService;
    private final DiemCongConsoleController diemCongConsoleController;
    private final BangQuyDoiConsoleController bangQuyDoiConsoleController;
    private final NguyenVongConsoleController nguyenVongConsoleController;
    private final NguyenVongXetTuyenService nguyenVongXetTuyenService;
    private final SpringDataXtDiemThiXetTuyenRepository diemThiRepository;

    public FullDatasetImportRunner(CandidateManagementService candidateManagementService,
                                   ScoreManagementService scoreManagementService,
                                   ToHopMonThiManagementService toHopMonThiManagementService,
                                   MajorManagementService majorManagementService,
                                   DiemCongConsoleController diemCongConsoleController,
                                   BangQuyDoiConsoleController bangQuyDoiConsoleController,
                                   NguyenVongConsoleController nguyenVongConsoleController,
                                   NguyenVongXetTuyenService nguyenVongXetTuyenService,
                                   SpringDataXtDiemThiXetTuyenRepository diemThiRepository) {
        this.candidateManagementService = candidateManagementService;
        this.scoreManagementService = scoreManagementService;
        this.toHopMonThiManagementService = toHopMonThiManagementService;
        this.majorManagementService = majorManagementService;
        this.diemCongConsoleController = diemCongConsoleController;
        this.bangQuyDoiConsoleController = bangQuyDoiConsoleController;
        this.nguyenVongConsoleController = nguyenVongConsoleController;
        this.nguyenVongXetTuyenService = nguyenVongXetTuyenService;
        this.diemThiRepository = diemThiRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        long startedAt = System.currentTimeMillis();

        System.out.println("[IMPORT] Starting full dataset import...");

        importMajors();
        importCombinations();
        importCandidates();
        importScores();
        importBangQuyDoi();
        importDiemCong();
        importNguyenVong();

        var rebuildSummary = nguyenVongXetTuyenService.rebuildAdmissionResults();
        majorManagementService.updateMajorStatsFromNguyenVong();

        long duration = System.currentTimeMillis() - startedAt;
        System.out.println("[IMPORT] Rebuild summary: " + rebuildSummary);
        System.out.println("[IMPORT] Full dataset import finished in " + duration + " ms");
    }

    private void importMajors() {
        majorManagementService.importExcelFile(Path.of(BASE, "Chi tieu 2025.xlsx"));
        majorManagementService.importExcelFile(Path.of(BASE, "Nguong dau vao 2025.xlsx"));
    }

    private void importCombinations() {
        toHopMonThiManagementService.deleteAllToHop();
        toHopMonThiManagementService.importExcelFile(Path.of(BASE, "tohopmon.xlsx"));
    }

    private void importCandidates() throws Exception {
        candidateManagementService.deleteAllCandidates();
        try (InputStream inputStream = new FileInputStream(Path.of(BASE, "Ds thi sinh.xlsx").toFile())) {
            List<XtThiSinhXetTuyen25Entity> candidates = candidateManagementService.importExcelCandidates(inputStream);
            candidateManagementService.saveBatch(candidates);
            System.out.println("[IMPORT] Candidates: " + candidates.size());
        }
    }

    private void importScores() {
        diemThiRepository.deleteAll();
        scoreManagementService.importExcel(new File(Path.of(BASE, "Ds thi sinh.xlsx").toString()));
        scoreManagementService.importExcel(new File(Path.of(BASE, "Diem DGNL VSAT - 0908.xlsx").toString()));
        System.out.println("[IMPORT] Scores: " + diemThiRepository.count());
    }

    private void importBangQuyDoi() throws Exception {
        bangQuyDoiConsoleController.deleteAll();
        int imported = bangQuyDoiConsoleController.importExcelFile(new File(Path.of(BASE, "BangQuyDoi", "BangQuyDoi.xlsx").toString()));
        System.out.println("[IMPORT] Bang quy doi: " + imported);
    }

    private void importDiemCong() throws Exception {
        diemCongConsoleController.deleteAll();
        var summary = diemCongConsoleController.importExcelFile(new File(Path.of(BASE, "Diem cong xet tuyen.xlsx").toString()));
        System.out.println("[IMPORT] Diem cong: " + summary.toMessage());
    }

    private void importNguyenVong() throws Exception {
        nguyenVongConsoleController.deleteAll();
        var summary = nguyenVongConsoleController.importExcelFileBatch(new File(Path.of(BASE, "Phan 8 - Nguyen Vong.xlsx").toString()), 2000);
        System.out.println("[IMPORT] Nguyen vong: " + summary.toMessage());
    }
}
