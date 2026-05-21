package com.example.admissions_management;

import com.example.admissions_management.application.dto.request.UserAccountRequest;
import com.example.admissions_management.application.service.CandidateManagementService;
import com.example.admissions_management.application.service.MajorManagementService;
import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.application.service.ToHopMonThiManagementService;
import com.example.admissions_management.application.service.UserAccountService;
import com.example.admissions_management.domain.model.UserRole;
import com.example.admissions_management.presentation.form.controller.DiemCongConsoleController;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(properties = {
        "app.swing.enabled=false",
        "app.swing.admin-console.enabled=false",
        "spring.docker.compose.enabled=false",
        "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver",
        "spring.main.lazy-initialization=true"
})
class ManualSampleDataImportTest {

    @Autowired
    private CandidateManagementService candidateManagementService;

    @Autowired
    private MajorManagementService majorManagementService;

    @Autowired
    private ToHopMonThiManagementService toHopMonThiManagementService;

    @Autowired
    private DiemCongConsoleController diemCongConsoleController;

    @Autowired
    private NguyenVongXetTuyenService nguyenVongXetTuyenService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void importSampleDataIntoLocalDockerDb() throws Exception {
        Path docs = Path.of("src", "main", "java", "com", "example", "admissions_management", "shared", "docs", "excels");

        clearTables();
        seedSampleUser();

        Path toHopFile = docs.resolve("tohopmon.xlsx");
        Path majorQuotaFile = docs.resolve("Chi tieu 2025.xlsx");
        Path majorThresholdFile = docs.resolve("Nguong dau vao 2025.xlsx");
        Path candidateFile = docs.resolve("Ds thi sinh.xlsx");
        Path diemCongFile = docs.resolve("Diem cong xet tuyen.xlsx");
        Path nguyenVongFile = docs.resolve("Phan 8 - Nguyen Vong.xlsx");

        System.out.println("Import tổ hợp từ: " + toHopFile);
        var toHopResult = toHopMonThiManagementService.importExcelFile(toHopFile);
        System.out.println("Tổ hợp mới: " + toHopResult.getImportedCount() + ", cập nhật: " + toHopResult.getUpdatedCount());

        System.out.println("Import ngành từ: " + majorQuotaFile);
        var majorQuotaResult = majorManagementService.importExcelFile(majorQuotaFile);
        System.out.println("Ngành từ chỉ tiêu - mới: " + majorQuotaResult.getImportedCount() + ", cập nhật: " + majorQuotaResult.getUpdatedCount());

        System.out.println("Import ngành bổ sung từ: " + majorThresholdFile);
        var majorThresholdResult = majorManagementService.importExcelFile(majorThresholdFile);
        System.out.println("Ngành từ ngưỡng - mới: " + majorThresholdResult.getImportedCount() + ", cập nhật: " + majorThresholdResult.getUpdatedCount());

        try (InputStream inputStream = Files.newInputStream(candidateFile)) {
            candidateManagementService.importExcelAndSave(inputStream);
        }
        System.out.println("Đã import thí sinh từ: " + candidateFile);

        var diemCongSummary = diemCongConsoleController.importExcelFile(diemCongFile.toFile());
        System.out.println("Điểm cộng - tổng: " + diemCongSummary.getTotalRows()
                + ", mới: " + diemCongSummary.getNewCount()
                + ", cập nhật: " + diemCongSummary.getUpdatedCount()
                + ", bỏ qua: " + diemCongSummary.getSkippedCount());

        int importedNguyenVong = importNguyenVongFromExcel(nguyenVongFile);
        System.out.println("Nguyện vọng đã import: " + importedNguyenVong);

        nguyenVongXetTuyenService.performSelectionForAllMajors();
        printCounts();
    }

    private void seedSampleUser() {
        if (!userAccountService.getAllUsers().isEmpty()) {
            return;
        }

        UserAccountRequest request = new UserAccountRequest();
        request.setUsername("admin");
        request.setFullName("Quản trị hệ thống");
        request.setPassword("admin123");
        request.setRole(UserRole.ADMIN);
        request.setEnabled(true);
        userAccountService.save(request);
    }

    private void clearTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE xt_nguyenvongxettuyen");
        jdbcTemplate.execute("TRUNCATE TABLE xt_diemcongxetuyen");
        jdbcTemplate.execute("TRUNCATE TABLE xt_thisinhxettuyen25");
        jdbcTemplate.execute("TRUNCATE TABLE xt_nganh");
        jdbcTemplate.execute("TRUNCATE TABLE xt_tohop_monthi");
        jdbcTemplate.execute("TRUNCATE TABLE user_account");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private int importNguyenVongFromExcel(Path path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(path);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet("xt_nguyenvongxettuyen");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerMap = buildHeaderMap(sheet.getRow(0), formatter);
            int imported = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                String nnCccd = value(row, headerMap, formatter, "nn_cccd");
                String maNganh = value(row, headerMap, formatter, "nv_manganh");
                Integer nvThuTu = intValue(value(row, headerMap, formatter, "nv_tt"));
                BigDecimal diemThxt = decimalValue(value(row, headerMap, formatter, "diem_thxt"));
                BigDecimal diemUtqd = decimalValue(value(row, headerMap, formatter, "diem_utqd"));
                String nvKeys = value(row, headerMap, formatter, "nv_keys");
                String maToHop = extractMaToHop(nvKeys);

                if (isBlank(nnCccd) || isBlank(maNganh) || nvThuTu == null) {
                    continue;
                }

                var created = nguyenVongXetTuyenService.createNguyenVong(
                        nnCccd.trim(),
                        maNganh.trim(),
                        maToHop,
                        nvThuTu
                );
                nguyenVongXetTuyenService.calculateScore(
                        created.getId(),
                        diemThxt == null ? BigDecimal.ZERO : diemThxt,
                        diemUtqd == null ? BigDecimal.ZERO : diemUtqd
                );
                imported++;
            }

            return imported;
        }
    }

    private Map<String, Integer> buildHeaderMap(Row row, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            String key = formatter.formatCellValue(row.getCell(i)).trim().toLowerCase();
            if (!key.isBlank()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private String value(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String key) {
        Integer index = headerMap.get(key.toLowerCase());
        if (index == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            if (!formatter.formatCellValue(row.getCell(i)).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Integer intValue(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim().replace(",", ".")).intValue();
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal decimalValue(String raw) {
        if (isBlank(raw)) {
            return BigDecimal.ZERO;
        }
        try {
            String normalized = raw.trim().replace(" ", "");
            if (normalized.contains(",") && normalized.contains(".")) {
                int comma = normalized.lastIndexOf(',');
                int dot = normalized.lastIndexOf('.');
                if (comma > dot) {
                    normalized = normalized.replace(".", "").replace(',', '.');
                } else {
                    normalized = normalized.replace(",", "");
                }
            } else {
                normalized = normalized.replace(',', '.');
            }
            return new BigDecimal(normalized);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private String extractMaToHop(String nvKeys) {
        if (isBlank(nvKeys)) {
            return "";
        }

        String[] parts = nvKeys.split("_");
        if (parts.length < 4) {
            return "";
        }

        return parts[parts.length - 2].trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void printCounts() {
        System.out.println("==== COUNTS AFTER IMPORT ====");
        printCount("user_account");
        printCount("xt_thisinhxettuyen25");
        printCount("xt_nganh");
        printCount("xt_tohop_monthi");
        printCount("xt_diemcongxetuyen");
        printCount("xt_nguyenvongxettuyen");
    }

    private void printCount(String table) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        System.out.println(table + " = " + count);
    }
}
