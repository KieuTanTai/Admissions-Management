package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.presentation.web.model.CandidateForm;
import com.example.admissions_management.presentation.web.model.CandidateImportResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

@Service
public class CandidateManagementService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CandidateRepository candidateRepository;

    public CandidateManagementService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    // ================= SEARCH =================

    public Page<XtThiSinhXetTuyen25Entity> searchCandidates(String query, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        if (!StringUtils.hasText(query)) {
            return candidateRepository.findAll(pageable);
        }
        String q = query.trim();
        return candidateRepository
                .findByCccdContainingIgnoreCaseOrHoContainingIgnoreCaseOrTenContainingIgnoreCase(q, q, q, pageable);
    }

    public Optional<XtThiSinhXetTuyen25Entity> findById(Integer id) {
        return candidateRepository.findById(id);
    }

    // ================= SAVE =================

    public XtThiSinhXetTuyen25Entity saveCandidate(CandidateForm form) {
        XtThiSinhXetTuyen25Entity entity =
                form.getId() != null
                        ? candidateRepository.findById(form.getId()).orElse(new XtThiSinhXetTuyen25Entity())
                        : candidateRepository.findByCccd(form.getCccd()).orElse(new XtThiSinhXetTuyen25Entity());

        applyFormToEntity(form, entity);
        return candidateRepository.save(entity);
    }

    public void deleteCandidate(Integer id) {
        candidateRepository.deleteById(id);
    }

    public void deleteAllCandidates() {
        candidateRepository.deleteAll();
    }

    // ================= IMPORT (TỐI ƯU) =================

    public CandidateImportResult importMultipleFiles(MultipartFile[] files) {

        CandidateImportResult result = new CandidateImportResult();
        Map<String, XtThiSinhXetTuyen25Entity> map = new HashMap<>();

        // 1. Đọc file → đưa vào map
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {
                if (file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                    readExcelToMap(file, map, result);
                } else {
                    readCsvToMap(file, map, result);
                }
            } catch (Exception e) {
                result.addError("Lỗi file " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        // 2. Query DB 1 lần duy nhất
        List<String> cccdList = new ArrayList<>(map.keySet());

        Map<String, XtThiSinhXetTuyen25Entity> dbMap = new HashMap<>();
        candidateRepository.findByCccdIn(cccdList)
                .forEach(e -> dbMap.put(e.getCccd(), e));

        // 3. Chuẩn bị batch save
        List<XtThiSinhXetTuyen25Entity> toSave = new ArrayList<>();

        for (XtThiSinhXetTuyen25Entity entity : map.values()) {
            XtThiSinhXetTuyen25Entity existing = dbMap.get(entity.getCccd());

            if (existing != null) {
                mergeEntity(existing, entity);
                existing.setUpdatedAt(LocalDate.now());
                toSave.add(existing);
                result.incrementUpdatedCount();
            } else {
                entity.setUpdatedAt(LocalDate.now());
                toSave.add(entity);
                result.incrementImportedCount();
            }
        }

        // 4. SAVE 1 LẦN (QUAN TRỌNG)
        candidateRepository.saveAll(toSave);

        result.setMessage("Import hoàn tất: "
                + result.getImportedCount() + " mới, "
                + result.getUpdatedCount() + " cập nhật.");

        return result;
    }

    // ================= READ FILE =================

    private void readExcelToMap(MultipartFile file,
                           Map<String, XtThiSinhXetTuyen25Entity> map,
                           CandidateImportResult result) throws Exception {

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerIndex = parseExcelHeader(headerRow);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String cccd = getExcelCell(row, headerIndex.get("cccd"));
            if (!StringUtils.hasText(cccd)) continue;

            XtThiSinhXetTuyen25Entity temp = new XtThiSinhXetTuyen25Entity();
            applyExcelRowToEntity(row, headerIndex, temp);

            map.put(cccd, temp);
        }
    }
}

    private void readCsvToMap(MultipartFile file,
                             Map<String, XtThiSinhXetTuyen25Entity> map,
                             CandidateImportResult result) throws Exception {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

        String headerLine = reader.readLine();
        Map<String, Integer> headerIndex = parseHeader(headerLine);

        String line;
        while ((line = reader.readLine()) != null) {
            String[] cells = splitCsvLine(line);
            String cccd = getCell(cells, headerIndex.get("cccd"));
            if (!StringUtils.hasText(cccd)) continue;

            XtThiSinhXetTuyen25Entity temp = new XtThiSinhXetTuyen25Entity();
            applyCsvCellsToEntity(cells, headerIndex, temp);

            map.put(cccd, temp);
        }
    }
    private String safe(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
}

    // ================= MERGE =================

    private void mergeEntity(XtThiSinhXetTuyen25Entity target,
                         XtThiSinhXetTuyen25Entity source) {

    if (isNotEmpty(source.getHo()))
        target.setHo(safe(source.getHo(), 100));

    if (isNotEmpty(source.getTen()))
        target.setTen(safe(source.getTen(), 50));

    if (isNotEmpty(source.getSoBaoDanh()))
        target.setSoBaoDanh(source.getSoBaoDanh());

    if (isNotEmpty(source.getNgaySinh()))
        target.setNgaySinh(source.getNgaySinh());

    if (isNotEmpty(source.getDienThoai()))
        target.setDienThoai(source.getDienThoai());

    if (isNotEmpty(source.getEmail()))
        target.setEmail(safe(source.getEmail(), 100));

    if (isNotEmpty(source.getGioiTinh()))
        target.setGioiTinh(source.getGioiTinh());

    if (isNotEmpty(source.getNoiSinh()))
        target.setNoiSinh(safe(source.getNoiSinh(), 100)); // ✅ FIX

    if (isNotEmpty(source.getKhuVuc()))
        target.setKhuVuc(source.getKhuVuc());

    if (isNotEmpty(source.getDoiTuong()))
        target.setDoiTuong(source.getDoiTuong());

    if (isNotEmpty(source.getPassword()))
        target.setPassword(source.getPassword());
}

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    // ================= PARSE =================

    private Map<String, Integer> parseExcelHeader(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String key = normalizeHeader(getExcelCell(headerRow, i));
            String field = mapHeaderToField(key);
            if (field != null) map.put(field, i);
        }
        return map;
    }

    private String getExcelCell(Row row, Integer cellIndex) {
    if (cellIndex == null) return null;

    try {
        var cell = row.getCell(cellIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
    return new java.text.DecimalFormat("0")
            .format(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    } catch (Exception e) {
        return null;
    }
}

    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = splitCsvLine(headerLine);
        for (int i = 0; i < headers.length; i++) {
            String field = mapHeaderToField(normalizeHeader(headers[i]));
            if (field != null) map.put(field, i);
        }
        return map;
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String getCell(String[] cells, Integer index) {
        if (index == null || index >= cells.length) return null;
        return cells[index].replace("\"", "").trim();
    }

    private String mapHeaderToField(String normalized) {
    return switch (normalized) {
        case "cccd" -> "cccd";

        case "hoten" -> "hoTen";

        case "sobaodanh", "sobd", "sbd" -> "soBaoDanh";

        case "ngaysinh" -> "ngaySinh";

        case "gioitinh" -> "gioiTinh";

        case "dtut", "doituong" -> "doiTuong";
        case "kvut", "khuvuc" -> "khuVuc";

        case "noisinh" -> "noiSinh";

        // ⚠️ QUAN TRỌNG: bỏ qua STT (không map)
        case "stt" -> null;

        default -> null;
    };
}

    private String normalizeHeader(String s) {
    if (s == null) return "";

    String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

    return noAccent.toLowerCase().replaceAll("[^a-z0-9]", "");
}

    private void applyExcelRowToEntity(Row row, Map<String, Integer> h,
                                   XtThiSinhXetTuyen25Entity e) {

    // ✅ CCCD
    e.setCccd(trim(getExcelCell(row, h.get("cccd"))));

    // ✅ HỌ TÊN GỘP
    String hoTen = trim(getExcelCell(row, h.get("hoTen")));
    if (hoTen != null) {
        String[] parts = hoTen.split("\\s+");
        if (parts.length > 1) {
            e.setTen(parts[parts.length - 1]);
            e.setHo(String.join(" ", Arrays.copyOf(parts, parts.length - 1)));
        } else {
            e.setTen(hoTen);
        }
    }

    // ❌ KHÔNG set lại ho/ten nữa

    e.setSoBaoDanh(trim(getExcelCell(row, h.get("soBaoDanh"))));
    e.setNgaySinh(trim(getExcelCell(row, h.get("ngaySinh"))));
    e.setDienThoai(trim(getExcelCell(row, h.get("dienThoai"))));
    e.setGioiTinh(trim(getExcelCell(row, h.get("gioiTinh"))));
    e.setEmail(trim(getExcelCell(row, h.get("email"))));
    e.setNoiSinh(safe(trim(getExcelCell(row, h.get("noiSinh"))), 100));
    e.setDoiTuong(trim(getExcelCell(row, h.get("doiTuong"))));
    e.setKhuVuc(trim(getExcelCell(row, h.get("khuVuc"))));
    e.setPassword(trim(getExcelCell(row, h.get("password"))));
}

    private void applyCsvCellsToEntity(String[] c, Map<String, Integer> h,
                                   XtThiSinhXetTuyen25Entity e) {

    e.setCccd(trim(getCell(c, h.get("cccd"))));

    String hoTen = trim(getCell(c, h.get("hoTen")));
    if (hoTen != null) {
        String[] parts = hoTen.split("\\s+");
        if (parts.length > 1) {
            e.setTen(parts[parts.length - 1]);
            e.setHo(String.join(" ", Arrays.copyOf(parts, parts.length - 1)));
        } else {
            e.setTen(hoTen);
        }
    }

    e.setSoBaoDanh(trim(getCell(c, h.get("soBaoDanh"))));
    e.setNgaySinh(trim(getCell(c, h.get("ngaySinh"))));
    e.setDienThoai(trim(getCell(c, h.get("dienThoai"))));
    e.setGioiTinh(trim(getCell(c, h.get("gioiTinh"))));
    e.setEmail(trim(getCell(c, h.get("email"))));

    // ✅ FIX CHÍNH Ở ĐÂY
    e.setNoiSinh(safe(trim(getCell(c, h.get("noiSinh"))), 100));

    e.setDoiTuong(trim(getCell(c, h.get("doiTuong"))));
    e.setKhuVuc(trim(getCell(c, h.get("khuVuc"))));
    e.setPassword(trim(getCell(c, h.get("password"))));
}

    private void applyFormToEntity(CandidateForm f, XtThiSinhXetTuyen25Entity e) {
        e.setCccd(trim(f.getCccd()));
        e.setSoBaoDanh(trim(f.getSoBaoDanh()));
        e.setHo(trim(f.getHo()));
        e.setTen(trim(f.getTen()));
        e.setNgaySinh(trim(f.getNgaySinh()));
        e.setDienThoai(trim(f.getDienThoai()));
        e.setGioiTinh(trim(f.getGioiTinh()));
        e.setEmail(trim(f.getEmail()));
        e.setNoiSinh(trim(f.getNoiSinh()));
        e.setDoiTuong(trim(f.getDoiTuong()));
        e.setKhuVuc(trim(f.getKhuVuc()));
        e.setPassword(trim(f.getPassword()));
        e.setUpdatedAt(LocalDate.now());
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}