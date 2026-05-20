package com.example.admissions_management.config;

import com.example.admissions_management.application.service.MajorManagementService;
import com.example.admissions_management.domain.model.Combination;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class MajorDataBootstrapRunner implements CommandLineRunner {

    private static final String MAJOR_FILE = "src/main/java/com/example/admissions_management/shared/docs/excels/Chi tieu 2025.xlsx";
    private static final String COMBINATION_FILE = "src/main/java/com/example/admissions_management/shared/docs/excels/tohopmon.xlsx";

    private final MajorRepository majorRepository;
    private final ICombinationRepository combinationRepository;
    private final MajorManagementService majorManagementService;

    public MajorDataBootstrapRunner(MajorRepository majorRepository,
                                    ICombinationRepository combinationRepository,
                                    MajorManagementService majorManagementService) {
        this.majorRepository = majorRepository;
        this.combinationRepository = combinationRepository;
        this.majorManagementService = majorManagementService;
    }

    @Override
    public void run(String... args) throws Exception {
        importMajorsIfNeeded();
        importCombinationsIfNeeded();
    }

    private void importMajorsIfNeeded() throws Exception {
        if (majorRepository.count() > 0) {
            return;
        }

        File file = new File(MAJOR_FILE);
        if (!file.exists()) {
            System.err.println("Major bootstrap file not found: " + MAJOR_FILE);
            return;
        }

        majorManagementService.importExcelFile(Path.of(MAJOR_FILE));
        System.out.println("Bootstrapped major data from: " + MAJOR_FILE);
    }

    private void importCombinationsIfNeeded() throws Exception {
        if (!combinationRepository.findAll().isEmpty()) {
            return;
        }

        File file = new File(COMBINATION_FILE);
        if (!file.exists()) {
            System.err.println("Combination bootstrap file not found: " + COMBINATION_FILE);
            return;
        }

        List<Combination> combinations = readCombinationExcel(file);
        for (Combination combination : combinations) {
            combinationRepository.save(combination);
        }

        System.out.println("Bootstrapped combination data from: " + COMBINATION_FILE + " | rows=" + combinations.size());
    }

    private List<Combination> readCombinationExcel(File file) throws Exception {
        List<Combination> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return result;
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return result;
            }

            Map<String, Integer> headers = buildHeaderIndex(headerRow, formatter);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                String maNganh = getValue(row, headers, formatter, "manganh");
                String maToHop = getValue(row, headers, formatter, "matohop");
                if (isBlank(maNganh) || isBlank(maToHop)) {
                    continue;
                }

                Combination combination = new Combination();
                combination.setMaNganh(maNganh.trim());
                combination.setMaToHop(maToHop.trim());
                combination.setThMon1(getValue(row, headers, formatter, "thmon1", "th_mon1"));
                combination.setHsMon1(toByte(getValue(row, headers, formatter, "hsmon1")));
                combination.setThMon2(getValue(row, headers, formatter, "thmon2", "th_mon2"));
                combination.setHsMon2(toByte(getValue(row, headers, formatter, "hsmon2")));
                combination.setThMon3(getValue(row, headers, formatter, "thmon3", "th_mon3"));
                combination.setHsMon3(toByte(getValue(row, headers, formatter, "hsmon3")));
                combination.setTbKeys(defaultIfBlank(getValue(row, headers, formatter, "tbkeys", "tb_keys"), combination.getMaNganh() + "_" + combination.getMaToHop()));
                combination.setN1(parseFlag(getValue(row, headers, formatter, "n1")));
                combination.setTo(parseFlag(getValue(row, headers, formatter, "to")));
                combination.setLi(parseFlag(getValue(row, headers, formatter, "li")));
                combination.setHo(parseFlag(getValue(row, headers, formatter, "ho")));
                combination.setSi(parseFlag(getValue(row, headers, formatter, "si")));
                combination.setVa(parseFlag(getValue(row, headers, formatter, "va")));
                combination.setSu(parseFlag(getValue(row, headers, formatter, "su")));
                combination.setDi(parseFlag(getValue(row, headers, formatter, "di")));
                combination.setTi(parseFlag(getValue(row, headers, formatter, "ti")));
                combination.setKhac(parseFlag(getValue(row, headers, formatter, "khac")));
                combination.setKtpl(parseFlag(getValue(row, headers, formatter, "ktpl")));
                combination.setDoLech(toBigDecimal(getValue(row, headers, formatter, "dolech", "dolech")));
                result.add(combination);
            }
        }

        return result;
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new LinkedHashMap<>();
        short lastCell = headerRow.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            String value = getCellStringValue(headerRow, i, formatter);
            String key = normalizeHeader(value);
            if (!key.isEmpty() && !map.containsKey(key)) {
                map.put(key, i);
            }
        }
        return map;
    }

    private String getValue(Row row, Map<String, Integer> headers, DataFormatter formatter, String... keys) {
        for (String key : keys) {
            Integer idx = headers.get(normalizeHeader(key));
            if (idx != null) {
                return getCellStringValue(row, idx, formatter);
            }
        }
        return "";
    }

    private String getCellStringValue(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell);
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null) {
                String text = formatter.formatCellValue(cell);
                if (!isBlank(text)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim().toLowerCase(Locale.ROOT);
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return text.replaceAll("[^a-z0-9]", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private Byte toByte(String value) {
        try {
            if (isBlank(value)) {
                return null;
            }
            return Byte.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Boolean parseFlag(String value) {
        if (isBlank(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return !("0".equals(normalized) || "false".equals(normalized) || "no".equals(normalized));
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            if (isBlank(value)) {
                return null;
            }
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (Exception ex) {
            return null;
        }
    }
}
