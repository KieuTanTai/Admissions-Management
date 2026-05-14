package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtToHopMonThiEntity;
import com.example.admissions_management.infrastructure.persistence.repository.ToHopMonThiRepository;
import com.example.admissions_management.presentation.form.model.ToHopMonThiForm;
import com.example.admissions_management.presentation.form.model.ToHopMonThiImportResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ToHopMonThiManagementService {

    private static final int PAGE_SIZE = 20;

    private final ToHopMonThiRepository repository;

    public ToHopMonThiManagementService(ToHopMonThiRepository repository) {
        this.repository = repository;
    }

    public List<XtToHopMonThiEntity> searchToHop(String query, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<XtToHopMonThiEntity> results;

        if (query == null || query.isBlank()) {
            results = repository.findAll(pageable);
        } else {
            results = repository.findByMaToHopContainingIgnoreCaseOrTenToHopContainingIgnoreCase(
                    query.trim(),
                    query.trim(),
                    pageable
            );
        }

        return results.get().collect(Collectors.toList());
    }

    public Optional<XtToHopMonThiEntity> getById(Integer id) {
        return repository.findById(id);
    }

    public void saveToHop(ToHopMonThiForm form) {
        XtToHopMonThiEntity entity;

        if (form.getId() != null) {
            entity = repository.findById(form.getId()).orElse(new XtToHopMonThiEntity());
        } else if (form.getMaToHop() != null && !form.getMaToHop().isBlank()) {
            entity = repository.findByMaToHop(form.getMaToHop().trim()).orElse(new XtToHopMonThiEntity());
        } else {
            entity = new XtToHopMonThiEntity();
        }

        entity.setMaToHop(safe(form.getMaToHop(), 45));
        entity.setMon1(safe(form.getMon1(), 10));
        entity.setMon2(safe(form.getMon2(), 10));
        entity.setMon3(safe(form.getMon3(), 10));
        entity.setTenToHop(safe(form.getTenToHop(), 100));

        repository.save(entity);
    }

    public void deleteToHop(Integer id) {
        repository.deleteById(id);
    }

    public void deleteAllToHop() {
        repository.deleteAll();
    }

    public ToHopMonThiImportResult importExcelFile(Path path) {
        ToHopMonThiImportResult result = new ToHopMonThiImportResult();

        try (InputStream inputStream = Files.newInputStream(path)) {
            List<XtToHopMonThiEntity> list = readExcel(inputStream);

            if (list.isEmpty()) {
                result.addError("File không đọc được tổ hợp nào. Kiểm tra header MA_TO_HOP / TEN_TO_HOP.");
                return result;
            }

            saveImported(list, result);
        } catch (Exception ex) {
            result.addError("Lỗi import file " + path + ": " + ex.getMessage());
        }

        result.setMessage("Import hoàn tất: "
                + result.getImportedCount()
                + " mới, "
                + result.getUpdatedCount()
                + " cập nhật.");

        return result;
    }

    private List<XtToHopMonThiEntity> readExcel(InputStream inputStream) throws IOException {
        List<XtToHopMonThiEntity> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new IOException("Không tìm thấy sheet.");
            }

            DataFormatter formatter = new DataFormatter();
            int headerRowIndex = findHeaderRow(sheet, formatter);

            if (headerRowIndex < 0) {
                throw new IOException("Không tìm thấy dòng header có cột MA_TO_HOP.");
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter);

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                XtToHopMonThiEntity entity = parseRow(row, headerIndex, formatter);

                if (entity != null) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        int maxScanRows = Math.min(sheet.getLastRowNum(), 30);

        for (int i = sheet.getFirstRowNum(); i <= maxScanRows; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(row, formatter);

            if (containsAnyKey(
                    headerIndex,
                    "matohop",
                    "ma to hop",
                    "mã tổ hợp",
                    "ma_to_hop",
                    "tentohop",
                    "ten_to_hop"
            )) {
                return i;
            }
        }

        return -1;
    }

    private XtToHopMonThiEntity parseRow(Row row, Map<String, Integer> headerIndex, DataFormatter formatter) {
        String rawMaToHop = getValue(row, headerIndex, formatter,
                "matohop",
                "ma to hop",
                "mã tổ hợp",
                "ma_to_hop"
        );

        String tenToHopCode = getValue(row, headerIndex, formatter,
                "tentohop",
                "ten to hop",
                "tên tổ hợp",
                "ten_to_hop"
        );

        String maToHop = defaultIfBlank(tenToHopCode, extractToHopCode(rawMaToHop));

        if (maToHop == null || maToHop.isBlank()) {
            return null;
        }

        String[] mons = parseSubjects(rawMaToHop);

        if (mons[0] == null || mons[1] == null || mons[2] == null) {
            return null;
        }

        XtToHopMonThiEntity entity = new XtToHopMonThiEntity();

        entity.setMaToHop(safe(maToHop, 45));
        entity.setMon1(safe(mons[0], 10));
        entity.setMon2(safe(mons[1], 10));
        entity.setMon3(safe(mons[2], 10));
        entity.setTenToHop(safe(buildTenToHop(mons[0], mons[1], mons[2]), 100));

        return entity;
    }

    private void saveImported(List<XtToHopMonThiEntity> importedList, ToHopMonThiImportResult result) {
        Map<String, XtToHopMonThiEntity> importMap = new LinkedHashMap<>();

        for (XtToHopMonThiEntity imported : importedList) {
            if (imported.getMaToHop() == null || imported.getMaToHop().isBlank()) {
                continue;
            }

            importMap.put(imported.getMaToHop(), imported);
        }

        List<String> maToHopList = new ArrayList<>(importMap.keySet());
        Map<String, XtToHopMonThiEntity> existingMap = new HashMap<>();

        List<XtToHopMonThiEntity> existingList = repository.findByMaToHopIn(maToHopList);

        for (XtToHopMonThiEntity entity : existingList) {
            existingMap.put(entity.getMaToHop(), entity);
        }

        List<XtToHopMonThiEntity> toSave = new ArrayList<>();

        for (Map.Entry<String, XtToHopMonThiEntity> entry : importMap.entrySet()) {
            String maToHop = entry.getKey();
            XtToHopMonThiEntity imported = entry.getValue();

            XtToHopMonThiEntity entity = existingMap.get(maToHop);
            boolean isUpdate = entity != null;

            if (!isUpdate) {
                entity = new XtToHopMonThiEntity();
            }

            entity.setMaToHop(imported.getMaToHop());
            entity.setMon1(imported.getMon1());
            entity.setMon2(imported.getMon2());
            entity.setMon3(imported.getMon3());
            entity.setTenToHop(imported.getTenToHop());

            toSave.add(entity);

            if (isUpdate) {
                result.incrementUpdatedCount();
            } else {
                result.incrementImportedCount();
            }
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
            repository.flush();
        }
    }

    private String[] parseSubjects(String raw) {
        if (raw == null || raw.isBlank()) {
            return new String[]{null, null, null};
        }

        int start = raw.indexOf("(");
        int end = raw.indexOf(")");

        if (start < 0 || end < 0 || end <= start) {
            return new String[]{null, null, null};
        }

        String inside = raw.substring(start + 1, end);
        String[] parts = inside.split(",");

        if (parts.length < 3) {
            return new String[]{null, null, null};
        }

        String mon1 = extractSubjectCode(parts[0]);
        String mon2 = extractSubjectCode(parts[1]);
        String mon3 = extractSubjectCode(parts[2]);

        return new String[]{mon1, mon2, mon3};
    }

    private String extractSubjectCode(String part) {
        if (part == null) {
            return null;
        }

        String cleaned = part.trim();

        int dash = cleaned.indexOf("-");

        if (dash > 0) {
            return cleaned.substring(0, dash).trim();
        }

        return cleaned;
    }

    private String extractToHopCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw.trim();

        int index = cleaned.indexOf("(");

        if (index > 0) {
            return cleaned.substring(0, index).trim();
        }

        return cleaned;
    }

    private String buildTenToHop(String mon1, String mon2, String mon3) {
        return subjectName(mon1) + ", " + subjectName(mon2) + ", " + subjectName(mon3);
    }

    private String subjectName(String code) {
        if (code == null) {
            return "";
        }

        return switch (code.trim().toUpperCase()) {
            case "TO" -> "Toán";
            case "LI" -> "Vật lí";
            case "HO" -> "Hóa học";
            case "SI" -> "Sinh học";
            case "VA" -> "Ngữ văn";
            case "SU" -> "Lịch sử";
            case "DI" -> "Địa lí";
            case "N1" -> "Tiếng Anh";
            case "GD" -> "GDCD";
            case "NK1" -> "Kể chuyện - Đọc diễn cảm";
            case "NK2" -> "Hát - Nhạc";
            case "NK3" -> "Hình họa";
            case "NK4" -> "Trang trí";
            case "NK5" -> "Hát - Nhạc cụ";
            case "NK6" -> "Xướng âm - Thẩm âm, Tiết tấu";
            case "KTPL" -> "KTPL";
            case "CNCN" -> "CNCN";
            case "CNNN" -> "CNNN";
            default -> code;
        };
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();

        if (headerRow == null) {
            return map;
        }

        short lastCellNum = headerRow.getLastCellNum();

        if (lastCellNum < 0) {
            return map;
        }

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i);
            String header = formatter.formatCellValue(cell);
            String normalized = normalizeHeader(header);

            if (!normalized.isBlank()) {
                map.put(normalized, i);
            }
        }

        return map;
    }

    private boolean containsAnyKey(Map<String, Integer> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(normalizeHeader(key))) {
                return true;
            }
        }

        return false;
    }

    private String getValue(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, String... keys) {
        for (String key : keys) {
            Integer index = headerIndex.get(normalizeHeader(key));

            if (index != null) {
                Cell cell = row.getCell(index);
                return clean(formatter.formatCellValue(cell));
            }
        }

        return null;
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        short lastCellNum = row.getLastCellNum();

        if (lastCellNum < 0) {
            return true;
        }

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);

            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }

        String normalized = Normalizer.normalize(
                header.trim().toLowerCase(),
                Normalizer.Form.NFD
        );

        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^a-z0-9]", "");

        return normalized;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        value = value.replace("\"", "").trim();

        return value.isEmpty() ? null : value;
    }

    private String safe(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}