package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemThiXetTuyenRepository;
import com.example.admissions_management.presentation.web.model.ScoreManagementForm;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ScoreManagementService {

    private static final List<String> SUBJECT_CODES = List.of(
            "TO", "LI", "HO", "SI", "SU", "DI", "VA", "N1_THI", "N1_CC", "CNCN", "CNNN", "TI", "KTPL", "NL1", "NK1", "NK2");

        private static final Map<String, List<String>> THPT_COMBINATION_FIELDS = Map.of(
            "A00", List.of("TO", "LI", "HO"),
            "A01", List.of("TO", "LI", "N1_THI"),
            "C00", List.of("VA", "SU", "DI"),
            "D01", List.of("TO", "VA", "N1_THI"));

    private static final Map<String, String> SUBJECT_LABELS = new LinkedHashMap<>();

    static {
        SUBJECT_LABELS.put("TO", "Toan (TO)");
        SUBJECT_LABELS.put("LI", "Vat Ly (LI)");
        SUBJECT_LABELS.put("HO", "Hoa Hoc (HO)");
        SUBJECT_LABELS.put("SI", "Sinh Hoc (SI)");
        SUBJECT_LABELS.put("SU", "Lich Su (SU)");
        SUBJECT_LABELS.put("DI", "Dia Ly (DI)");
        SUBJECT_LABELS.put("VA", "Ngu Van (VA)");
        SUBJECT_LABELS.put("N1_THI", "Ngoai Ngu 1 Thi (N1_THI)");
        SUBJECT_LABELS.put("N1_CC", "Ngoai Ngu 1 Chung Chi (N1_CC)");
        SUBJECT_LABELS.put("CNCN", "Cong nghe cong nghiep (CNCN)");
        SUBJECT_LABELS.put("CNNN", "Cong nghe nong nghiep (CNNN)");
        SUBJECT_LABELS.put("TI", "Tin hoc (TI)");
        SUBJECT_LABELS.put("KTPL", "Kinh te va phap luat (KTPL)");
        SUBJECT_LABELS.put("NL1", "Diem DGNL (NL1)");
        SUBJECT_LABELS.put("NK1", "Nang khieu 1 (NK1)");
        SUBJECT_LABELS.put("NK2", "Nang khieu 2 (NK2)");
    }

    private final SpringDataXtDiemThiXetTuyenRepository repository;

    public ScoreManagementService(SpringDataXtDiemThiXetTuyenRepository repository) {
        this.repository = repository;
    }

    public Map<String, String> getSubjectLabels() {
        return SUBJECT_LABELS;
    }

    public List<XtDiemThiXetTuyenEntity> getAllRows() {
        return repository.findAllByOrderByIdDesc();
    }

    public List<XtDiemThiXetTuyenEntity> getByType(ScoreMethodType type) {
        return getAllRows().stream()
                .filter(row -> inferType(row.getdPhuongThuc()) == type)
                .toList();
    }

    public Optional<XtDiemThiXetTuyenEntity> findById(Integer id) {
        return repository.findById(id);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public XtDiemThiXetTuyenEntity save(ScoreManagementForm form) {
        XtDiemThiXetTuyenEntity entity = resolveTargetEntity(form);
        mapFormToEntity(form, entity);
        return repository.save(entity);
    }

    public ImportResult importExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            return importWorkbook(workbook);
        } catch (IOException exception) {
            return new ImportResult(0, 0, 0, "Khong the doc file Excel: " + exception.getMessage());
        }
    }

    public ImportResult importExcel(File file) {
        try (InputStream inputStream = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(inputStream)) {
            return importWorkbook(workbook);
        } catch (IOException exception) {
            return new ImportResult(0, 0, 0, "Khong the doc file Excel: " + exception.getMessage());
        }
    }

    private ImportResult importWorkbook(Workbook workbook) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        if (workbook.getNumberOfSheets() == 0) {
            return new ImportResult(inserted, updated, skipped, "Khong tim thay sheet nao trong file Excel.");
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return new ImportResult(inserted, updated, skipped, "Khong doc duoc sheet dau tien.");
        }

        DataFormatter formatter = new DataFormatter(Locale.US);
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return new ImportResult(inserted, updated, skipped, "File Excel khong co dong header.");
        }

        Map<String, Integer> headerIndex = extractHeaderIndex(headerRow, formatter);
        if (!headerIndex.containsKey("CCCD")) {
            return new ImportResult(inserted, updated, skipped,
                    "Header bat buoc: CCCD. Cac header hop le: CCCD, SOBAODANH, D_PHUONGTHUC, TO, LI, HO, SI, SU, DI, VA, N1_THI, N1_CC, CNCN, CNNN, TI, KTPL, NL1, NK1, NK2.");
        }

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isRowEmpty(row, formatter)) {
                skipped++;
                continue;
            }

            String cccd = readStringCell(row, headerIndex.get("CCCD"), formatter);
            if (cccd.isBlank()) {
                skipped++;
                continue;
            }

            String method = normalizeImportedMethod(readStringCell(row, headerIndex.get("D_PHUONGTHUC"), formatter));
            if (method.isBlank()) {
                skipped++;
                continue;
            }

            Optional<XtDiemThiXetTuyenEntity> existing = repository.findByCccd(cccd);
            XtDiemThiXetTuyenEntity entity = existing.orElseGet(XtDiemThiXetTuyenEntity::new);
            if (existing.isPresent()) {
                updated++;
            } else {
                inserted++;
            }

            BigDecimal to = readDecimalCell(row, headerIndex.get("TO"), formatter);
            BigDecimal li = readDecimalCell(row, headerIndex.get("LI"), formatter);
            BigDecimal ho = readDecimalCell(row, headerIndex.get("HO"), formatter);
            BigDecimal si = readDecimalCell(row, headerIndex.get("SI"), formatter);
            BigDecimal su = readDecimalCell(row, headerIndex.get("SU"), formatter);
            BigDecimal di = readDecimalCell(row, headerIndex.get("DI"), formatter);
            BigDecimal va = readDecimalCell(row, headerIndex.get("VA"), formatter);
            BigDecimal n1Thi = readDecimalCell(row, headerIndex.get("N1_THI"), formatter);
            BigDecimal n1Cc = readDecimalCell(row, headerIndex.get("N1_CC"), formatter);
            BigDecimal cncn = readDecimalCell(row, headerIndex.get("CNCN"), formatter);
            BigDecimal cnnn = readDecimalCell(row, headerIndex.get("CNNN"), formatter);
            BigDecimal ti = readDecimalCell(row, headerIndex.get("TI"), formatter);
            BigDecimal ktpl = readDecimalCell(row, headerIndex.get("KTPL"), formatter);
            BigDecimal nl1 = readDecimalCell(row, headerIndex.get("NL1"), formatter);
            BigDecimal nk1 = readDecimalCell(row, headerIndex.get("NK1"), formatter);
            BigDecimal nk2 = readDecimalCell(row, headerIndex.get("NK2"), formatter);

            entity.setCccd(cccd);
            entity.setSoBaoDanh(readStringCell(row, headerIndex.get("SOBAODANH"), formatter));
            entity.setdPhuongThuc(method);

            resetAllScoreColumns(entity);
            applyImportPolicy(entity, method, to, li, ho, si, su, di, va, n1Thi, n1Cc, cncn, cnnn, ti, ktpl, nl1, nk1, nk2);

            repository.save(entity);
        }

        return new ImportResult(inserted, updated, skipped,
                "Import Excel thanh cong: " + inserted + " them moi, " + updated + " cap nhat, " + skipped + " bo qua.");
    }

    public List<SubjectStatisticRow> summarizeByTypeForSubject(String subjectCode) {
        String normalizedSubject = normalizeSubjectCode(subjectCode);
        List<SubjectStatisticRow> rows = new ArrayList<>();

        for (ScoreMethodType type : ScoreMethodType.values()) {
            List<BigDecimal> values = getByType(type).stream()
                    .map(entity -> readSubjectScore(entity, normalizedSubject))
                    .filter(Objects::nonNull)
                    .toList();

            rows.add(new SubjectStatisticRow(
                    type.name(),
                    values.size(),
                    average(values),
                    minimum(values),
                    maximum(values)));
        }

        return rows;
    }

    public ScoreManagementForm toForm(XtDiemThiXetTuyenEntity entity) {
        ScoreManagementForm form = new ScoreManagementForm();
        form.setId(entity.getId());
        form.setCccd(entity.getCccd());
        form.setSoBaoDanh(entity.getSoBaoDanh());
        form.setdPhuongThuc(entity.getdPhuongThuc());
        form.setTo(entity.getTo());
        form.setLi(entity.getLi());
        form.setHo(entity.getHo());
        form.setSi(entity.getSi());
        form.setSu(entity.getSu());
        form.setDi(entity.getDi());
        form.setVa(entity.getVa());
        form.setN1Thi(entity.getN1Thi());
        form.setN1Cc(entity.getN1Cc());
        form.setCncn(entity.getCncn());
        form.setCnnn(entity.getCnnn());
        form.setTi(entity.getTi());
        form.setKtpl(entity.getKtpl());
        form.setNl1(entity.getNl1());
        form.setNk1(entity.getNk1());
        form.setNk2(entity.getNk2());
        return form;
    }

    public ScoreManagementForm createEmptyForm() {
        ScoreManagementForm form = new ScoreManagementForm();
        form.setdPhuongThuc("THPT_A00");
        return form;
    }

    private XtDiemThiXetTuyenEntity resolveTargetEntity(ScoreManagementForm form) {
        if (form.getId() != null) {
            Optional<XtDiemThiXetTuyenEntity> byId = repository.findById(form.getId());
            if (byId.isPresent()) {
                return byId.get();
            }
        }

        String cccd = safeTrim(form.getCccd());
        if (!cccd.isBlank()) {
            Optional<XtDiemThiXetTuyenEntity> byCccd = repository.findByCccd(cccd);
            if (byCccd.isPresent()) {
                return byCccd.get();
            }
        }

        return new XtDiemThiXetTuyenEntity();
    }

    private void mapFormToEntity(ScoreManagementForm form, XtDiemThiXetTuyenEntity entity) {
        entity.setCccd(safeTrim(form.getCccd()));
        entity.setSoBaoDanh(safeTrim(form.getSoBaoDanh()));
        entity.setdPhuongThuc(safeTrim(form.getdPhuongThuc()));
        entity.setTo(form.getTo());
        entity.setLi(form.getLi());
        entity.setHo(form.getHo());
        entity.setSi(form.getSi());
        entity.setSu(form.getSu());
        entity.setDi(form.getDi());
        entity.setVa(form.getVa());
        entity.setN1Thi(form.getN1Thi());
        entity.setN1Cc(form.getN1Cc());
        entity.setCncn(form.getCncn());
        entity.setCnnn(form.getCnnn());
        entity.setTi(form.getTi());
        entity.setKtpl(form.getKtpl());
        entity.setNl1(form.getNl1());
        entity.setNk1(form.getNk1());
        entity.setNk2(form.getNk2());
    }

    private Map<String, Integer> extractHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = normalizeHeader(formatter.formatCellValue(cell));
            if (!header.isBlank()) {
                map.put(header, cell.getColumnIndex());
            }
        }

        if (map.containsKey("TOAN") && !map.containsKey("TO")) {
            map.put("TO", map.get("TOAN"));
        }
        return map;
    }

    private String normalizeHeader(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "_");
    }

    private String normalizeImportedMethod(String value) {
        String normalized = normalizeHeader(value);
        if (normalized.startsWith("THPT_")) {
            String combination = normalized.substring("THPT_".length());
            return THPT_COMBINATION_FIELDS.containsKey(combination) ? "THPT_" + combination : "";
        }

        if (THPT_COMBINATION_FIELDS.containsKey(normalized)) {
            return "THPT_" + normalized;
        }

        if (normalized.contains("VSAT")) {
            return "VSAT";
        }

        if (normalized.contains("DGNL")) {
            return "DGNL";
        }

        return "";
    }

    private void resetAllScoreColumns(XtDiemThiXetTuyenEntity entity) {
        entity.setTo(BigDecimal.ZERO);
        entity.setLi(BigDecimal.ZERO);
        entity.setHo(BigDecimal.ZERO);
        entity.setSi(BigDecimal.ZERO);
        entity.setSu(BigDecimal.ZERO);
        entity.setDi(BigDecimal.ZERO);
        entity.setVa(BigDecimal.ZERO);
        entity.setN1Thi(BigDecimal.ZERO);
        entity.setN1Cc(BigDecimal.ZERO);
        entity.setCncn(BigDecimal.ZERO);
        entity.setCnnn(BigDecimal.ZERO);
        entity.setTi(BigDecimal.ZERO);
        entity.setKtpl(BigDecimal.ZERO);
        entity.setNl1(BigDecimal.ZERO);
        entity.setNk1(BigDecimal.ZERO);
        entity.setNk2(BigDecimal.ZERO);
    }

    private void applyImportPolicy(XtDiemThiXetTuyenEntity entity,
                                   String method,
                                   BigDecimal to,
                                   BigDecimal li,
                                   BigDecimal ho,
                                   BigDecimal si,
                                   BigDecimal su,
                                   BigDecimal di,
                                   BigDecimal va,
                                   BigDecimal n1Thi,
                                   BigDecimal n1Cc,
                                   BigDecimal cncn,
                                   BigDecimal cnnn,
                                   BigDecimal ti,
                                   BigDecimal ktpl,
                                   BigDecimal nl1,
                                   BigDecimal nk1,
                                   BigDecimal nk2) {
        if (method.startsWith("THPT_")) {
            List<String> fields = THPT_COMBINATION_FIELDS.get(method.substring("THPT_".length()));
            if (fields == null) {
                return;
            }

            if (fields.contains("TO")) {
                entity.setTo(zeroIfNull(to));
            }
            if (fields.contains("LI")) {
                entity.setLi(zeroIfNull(li));
            }
            if (fields.contains("HO")) {
                entity.setHo(zeroIfNull(ho));
            }
            if (fields.contains("SI")) {
                entity.setSi(zeroIfNull(si));
            }
            if (fields.contains("SU")) {
                entity.setSu(zeroIfNull(su));
            }
            if (fields.contains("DI")) {
                entity.setDi(zeroIfNull(di));
            }
            if (fields.contains("VA")) {
                entity.setVa(zeroIfNull(va));
            }
            if (fields.contains("N1_THI")) {
                entity.setN1Thi(zeroIfNull(n1Thi));
            }

            return;
        }

        if ("VSAT".equals(method)) {
            entity.setTo(zeroIfNull(to));
            entity.setLi(zeroIfNull(li));
            entity.setHo(zeroIfNull(ho));
            entity.setSi(zeroIfNull(si));
            entity.setSu(zeroIfNull(su));
            entity.setDi(zeroIfNull(di));
            entity.setVa(zeroIfNull(va));
            entity.setN1Thi(zeroIfNull(n1Thi));
            entity.setN1Cc(zeroIfNull(n1Cc));
            return;
        }

        if ("DGNL".equals(method)) {
            entity.setNl1(zeroIfNull(nl1));
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String readStringCell(Row row, Integer index, DataFormatter formatter) {
        if (index == null) {
            return "";
        }

        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private BigDecimal readDecimalCell(Row row, Integer index, DataFormatter formatter) {
        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        String value = formatter.formatCellValue(cell).trim();
        if (value.isBlank()) {
            return null;
        }

        String normalized = value.replace(",", "");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeSubjectCode(String subjectCode) {
        String normalized = safeTrim(subjectCode).toUpperCase(Locale.ROOT);
        if (SUBJECT_CODES.contains(normalized)) {
            return normalized;
        }
        return "TO";
    }

    private BigDecimal readSubjectScore(XtDiemThiXetTuyenEntity entity, String subjectCode) {
        return switch (subjectCode) {
            case "TO" -> entity.getTo();
            case "LI" -> entity.getLi();
            case "HO" -> entity.getHo();
            case "SI" -> entity.getSi();
            case "SU" -> entity.getSu();
            case "DI" -> entity.getDi();
            case "VA" -> entity.getVa();
            case "N1_THI" -> entity.getN1Thi();
            case "N1_CC" -> entity.getN1Cc();
            case "CNCN" -> entity.getCncn();
            case "CNNN" -> entity.getCnnn();
            case "TI" -> entity.getTi();
            case "KTPL" -> entity.getKtpl();
            case "NL1" -> entity.getNl1();
            case "NK1" -> entity.getNk1();
            case "NK2" -> entity.getNk2();
            default -> null;
        };
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }

        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal minimum(List<BigDecimal> values) {
        return values.stream().min(BigDecimal::compareTo).orElse(null);
    }

    private BigDecimal maximum(List<BigDecimal> values) {
        return values.stream().max(BigDecimal::compareTo).orElse(null);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public ScoreMethodType inferType(String dPhuongThuc) {
        String value = safeTrim(dPhuongThuc).toUpperCase(Locale.ROOT);

        if (value.contains("VSAT")) {
            return ScoreMethodType.VSAT;
        }

        if (value.contains("DGNL")) {
            return ScoreMethodType.DGNL;
        }

        return ScoreMethodType.THPT;
    }

    public enum ScoreMethodType {
        THPT,
        VSAT,
        DGNL
    }

    public record SubjectStatisticRow(String methodType, long count, BigDecimal average, BigDecimal minimum,
                                      BigDecimal maximum) {
    }

    public record ImportResult(int inserted, int updated, int skipped, String message) {
    }
}
