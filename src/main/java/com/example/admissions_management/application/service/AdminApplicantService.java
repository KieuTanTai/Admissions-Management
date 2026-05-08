package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.RegisterApplicantRequest;
import com.example.admissions_management.application.dto.response.ApplicantResponse;
import com.example.admissions_management.domain.model.Applicant;
import com.example.admissions_management.domain.repository.ApplicantRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemThiXetTuyenRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminApplicantService {

    private final ApplicantRepository applicantRepository;
    private final SpringDataXtDiemThiXetTuyenRepository scoreRepository;

    public AdminApplicantService(ApplicantRepository applicantRepository,
                                SpringDataXtDiemThiXetTuyenRepository scoreRepository) {
        this.applicantRepository = applicantRepository;
        this.scoreRepository = scoreRepository;
    }

    public List<ApplicantResponse> getAllApplicants() {
        return applicantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicantResponse registerApplicant(RegisterApplicantRequest request) {
        applicantRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("Applicant email already exists: " + request.getEmail());
        });

        Applicant saved = applicantRepository.save(new Applicant(
                null,
                request.getFullName(),
                request.getEmail(),
                request.getProgram()
        ));

        return toResponse(saved);
    }

    /**
     * Import applicants and scores from DS thi sinh.xlsx file
     * Excel columns: STT, CCCD, Họ Tên, ..., Chương trình học, NK1, NK2, ...
     */
    public ImportResult importFromExcel(File excelFile) {
        int applicantsCreated = 0;
        int applicantsUpdated = 0;
        int scoresCreated = 0;
        int scoresUpdated = 0;
        int skipped = 0;

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<Row> rows = getAllRows(sheet);
            
            System.out.println("[DEBUG] Total rows in sheet: " + rows.size());
            System.out.println("[DEBUG] First data row (index 1):");

            for (int i = 1; i < rows.size(); i++) {
                Row row = rows.get(i);
                
                if (i <= 2) {
                    System.out.println("[DEBUG] Row " + i + ": Cell[1]=" + readStringCell(row, 1, formatter));
                }
                
                try {
                    // Excel columns mapping:
                    String cccd = readStringCell(row, 1, formatter);           // Col 2: CCCD
                    String hoTen = readStringCell(row, 2, formatter);          // Col 3: Họ Tên
                    String chuongTrinh = readStringCell(row, 21, formatter);   // Col 22: Chương trình học

                    if (cccd == null || cccd.trim().isEmpty()) {
                        skipped++;
                        if (i <= 5) {
                            System.out.println("[DEBUG] Row " + i + " skipped: cccd=null or empty");
                        }
                        continue;
                    }

                    // Create or update applicant
                    String email = generateEmailFromCCCD(cccd);
                    Applicant applicant = applicantRepository.findByEmail(email)
                            .orElse(new Applicant(null, hoTen, email, chuongTrinh));

                    if (applicant.getId() == null) {
                        applicantRepository.save(applicant);
                        applicantsCreated++;
                    } else {
                        applicant.setFullName(hoTen);
                        applicant.setProgram(chuongTrinh);
                        applicantRepository.save(applicant);
                        applicantsUpdated++;
                    }

                    // Create or update score record
                    XtDiemThiXetTuyenEntity score = scoreRepository.findByCccd(cccd)
                            .orElse(new XtDiemThiXetTuyenEntity());

                    boolean isNewScore = score.getId() == null;

                    // Map Excel columns to score entity
                    score.setCccd(cccd);
                    score.setTo(readDecimalCell(row, 7, formatter));           // Col 8: TO
                    score.setVa(readDecimalCell(row, 8, formatter));           // Col 9: VA
                    score.setLi(readDecimalCell(row, 9, formatter));           // Col 10: LI
                    score.setHo(readDecimalCell(row, 10, formatter));          // Col 11: HO
                    score.setSi(readDecimalCell(row, 11, formatter));          // Col 12: SI
                    score.setSu(readDecimalCell(row, 12, formatter));          // Col 13: SU
                    score.setDi(readDecimalCell(row, 13, formatter));          // Col 14: DI
                    score.setKtpl(readDecimalCell(row, 17, formatter));        // Col 18: KTPL
                    score.setTi(readDecimalCell(row, 18, formatter));          // Col 19: TI
                    score.setCncn(readDecimalCell(row, 19, formatter));        // Col 20: CNCN
                    score.setCnnn(readDecimalCell(row, 20, formatter));        // Col 21: CNNN
                    score.setNk1(readDecimalCell(row, 22, formatter));         // Col 23: NK1
                    score.setNk2(readDecimalCell(row, 23, formatter));         // Col 24: NK2

                    scoreRepository.save(score);
                    if (isNewScore) {
                        scoresCreated++;
                    } else {
                        scoresUpdated++;
                    }

                } catch (Exception e) {
                    skipped++;
                    if (i <= 5) {
                        System.err.println("Error processing row " + (i + 1) + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("[DEBUG] Import complete: Created=" + applicantsCreated + ", Updated=" + applicantsUpdated + ", Skipped=" + skipped);
            return new ImportResult(applicantsCreated, applicantsUpdated, scoresCreated, scoresUpdated, skipped);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }
    }

    private String generateEmailFromCCCD(String cccd) {
        return "ts_" + cccd.toLowerCase() + "@admission.local";
    }

    private List<Row> getAllRows(org.apache.poi.ss.usermodel.Sheet sheet) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    private String readStringCell(Row row, int colIndex, DataFormatter formatter) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        
        // Try direct string value first
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            String value = cell.getStringCellValue();
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        }
        
        // For numeric cells, format them
        String formattedValue = formatter.formatCellValue(cell);
        return (formattedValue != null && !formattedValue.trim().isEmpty()) ? formattedValue.trim() : null;
    }

    private BigDecimal readDecimalCell(Row row, int colIndex, DataFormatter formatter) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            return new BigDecimal(cell.getNumericCellValue());
        } catch (Exception e) {
            String stringValue = formatter.formatCellValue(cell);
            if (stringValue == null || stringValue.isEmpty()) return null;
            try {
                return new BigDecimal(stringValue);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    // Import result DTO
    public static class ImportResult {
        public int applicantsCreated;
        public int applicantsUpdated;
        public int scoresCreated;
        public int scoresUpdated;
        public int skipped;

        public ImportResult(int applicantsCreated, int applicantsUpdated, 
                           int scoresCreated, int scoresUpdated, int skipped) {
            this.applicantsCreated = applicantsCreated;
            this.applicantsUpdated = applicantsUpdated;
            this.scoresCreated = scoresCreated;
            this.scoresUpdated = scoresUpdated;
            this.skipped = skipped;
        }

        @Override
        public String toString() {
            return String.format("Import DS thi sinh thành công: %d applicants created, %d applicants updated, " +
                    "%d scores created, %d scores updated, %d skipped.",
                    applicantsCreated, applicantsUpdated, scoresCreated, scoresUpdated, skipped);
        }
    }

    private ApplicantResponse toResponse(Applicant applicant) {
        return new ApplicantResponse(
                applicant.getId(),
                applicant.getFullName(),
                applicant.getEmail(),
                applicant.getProgram()
        );
    }
}
