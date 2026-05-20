package com.example.admissions_management.config;

import com.example.admissions_management.presentation.form.controller.NguyenVongConsoleController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConditionalOnProperty(prefix = "app", name = "importNguyenVong", havingValue = "true")
public class ImportNguyenVongRunner implements CommandLineRunner {

    private final NguyenVongConsoleController controller;

    @Value("${app.importFile:src/main/java/com/example/admissions_management/shared/docs/excels/Phan 8 - Nguyen Vong.xlsx}")
    private String importFilePath;

    @Value("${app.batchSize:2000}")
    private int batchSize;

    public ImportNguyenVongRunner(NguyenVongConsoleController controller) {
        this.controller = controller;
    }

    @Override
    public void run(String... args) throws Exception {
        File file = new File(importFilePath);
        if (!file.exists()) {
            System.err.println("Import file not found: " + importFilePath);
            return;
        }

        try {
            long start = System.currentTimeMillis();
            System.out.println("Starting import NguyenVong from: " + importFilePath + " with batchSize=" + batchSize);
            com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary = controller.importExcelFileBatch(file, batchSize);
            long tookMs = System.currentTimeMillis() - start;
            System.out.println(summary.toMessage());
            System.out.println("Import duration (ms): " + tookMs);
            // do not call System.exit to allow the Spring context to shut down gracefully
        } catch (Exception ex) {
            System.err.println("Import failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return;
        }
    }
}
