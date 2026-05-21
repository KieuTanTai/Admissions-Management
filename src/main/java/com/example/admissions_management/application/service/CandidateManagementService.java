package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.presentation.form.model.CandidateForm;
import com.example.admissions_management.presentation.form.model.CandidateImportResult;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidateManagementService {

    private static final int PAGE_SIZE = 20;

    private final CandidateRepository candidateRepository;
    private final EntityManager entityManager;

    public CandidateManagementService(CandidateRepository candidateRepository, EntityManager entityManager) {
        this.candidateRepository = candidateRepository;
        this.entityManager = entityManager;
    }

    public List<XtThiSinhXetTuyen25Entity> searchCandidates(String query, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<XtThiSinhXetTuyen25Entity> results;

        if (query == null || query.isBlank()) {
            results = candidateRepository.findAll(pageable);
        } else {
            results = candidateRepository
                    .findByCccdContainingIgnoreCaseOrHoContainingIgnoreCaseOrTenContainingIgnoreCase(
                            query.trim(),
                            query.trim(),
                            query.trim(),
                            pageable
                    );
        }

        return results.get().collect(Collectors.toList());
    }

    public Optional<XtThiSinhXetTuyen25Entity> getCandidateById(Integer id) {
        return candidateRepository.findById(id);
    }

    public long countCandidates() {
        return candidateRepository.count();
    }

    public Map<String, Long> countByDoiTuong() {
        return candidateRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        entity -> normalizeStatValue(entity.getDoiTuong()),
                        TreeMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> countByKhuVuc() {
        return candidateRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        entity -> normalizeStatValue(entity.getKhuVuc()),
                        TreeMap::new,
                        Collectors.counting()));
    }

    public void saveCandidate(CandidateForm form) {
        XtThiSinhXetTuyen25Entity entity;

        if (form.getId() != null) {
            entity = candidateRepository
                    .findById(form.getId())
                    .orElse(new XtThiSinhXetTuyen25Entity());
        } else if (form.getCccd() != null && !form.getCccd().isBlank()) {
            entity = candidateRepository
                    .findByCccd(form.getCccd().trim())
                    .orElse(new XtThiSinhXetTuyen25Entity());
        } else {
            entity = new XtThiSinhXetTuyen25Entity();
        }

        String cccd = safe(form.getCccd(), 50);

        entity.setCccd(cccd);
        entity.setSoBaoDanh(safe(defaultIfBlank(form.getSoBaoDanh(), generateSoBaoDanh(cccd)), 50));
        entity.setHo(safe(form.getHo(), 100));
        entity.setTen(safe(form.getTen(), 50));
        entity.setNgaySinh(safe(form.getNgaySinh(), 50));
        entity.setDienThoai(safe(defaultIfBlank(normalizePhone(form.getDienThoai()), generatePhone(cccd)), 20));
        entity.setGioiTinh(safe(form.getGioiTinh(), 20));
        entity.setEmail(safe(defaultIfBlank(form.getEmail(), generateEmail(cccd)), 100));
        entity.setNoiSinh(safe(form.getNoiSinh(), 45));
        entity.setDoiTuong(safe(form.getDoiTuong(), 45));
        entity.setKhuVuc(safe(form.getKhuVuc(), 45));
        entity.setPassword(safe(form.getPassword(), 100));
        entity.setUpdatedAt(LocalDate.now());

        candidateRepository.save(entity);
    }

    public void deleteCandidate(Integer candidateId) {
        candidateRepository.deleteById(candidateId);
    }

    public void deleteAllCandidates() {
        candidateRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE xt_thisinhxettuyen25 AUTO_INCREMENT = 1").executeUpdate();
    }

    public CandidateImportResult importMultipleFiles(List<String> files) {
        CandidateImportResult result = new CandidateImportResult();

        for (String file : files) {
            Path path = Path.of(file);

            if (!Files.exists(path)) {
                result.addError("File không tồn tại: " + file);
                continue;
            }

            String lowerName = file.toLowerCase();

            try {
                if (lowerName.endsWith(".csv")) {
                    importCsv(path, result);
                } else if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                    importExcel(path, result);
                } else {
                    result.addError("Định dạng file không hỗ trợ: " + file);
                }
            } catch (Exception ex) {
                result.addError("Lỗi import file " + file + ": " + ex.getMessage());
            }
        }

        result.setMessage(
                "Import hoàn tất: "
                        + result.getImportedCount()
                        + " mới, "
                        + result.getUpdatedCount()
                        + " cập nhật."
        );

        return result;
    }

    public CandidateImportResult importCsvFile(Path path) {
        CandidateImportResult result = new CandidateImportResult();

        try {
            importCsv(path, result);
        } catch (IOException ex) {
            result.addError("Lỗi đọc file CSV: " + path + " - " + ex.getMessage());
        }

        result.setMessage(
                "Import hoàn tất: "
                        + result.getImportedCount()
                        + " mới, "
                        + result.getUpdatedCount()
                        + " cập nhật."
        );

        return result;
    }

    private void importCsv(Path path, CandidateImportResult result) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        if (lines.isEmpty()) {
            result.addError("File rỗng: " + path);
            return;
        }

        String[] headers = splitCsvLine(lines.get(0));
        Map<String, Integer> headerIndex = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            headerIndex.put(normalizeHeader(headers[i]), i);
        }

        List<XtThiSinhXetTuyen25Entity> candidates = new ArrayList<>();

        for (int rowIndex = 1; rowIndex < lines.size(); rowIndex++) {
            String line = lines.get(rowIndex);

            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] columns = splitCsvLine(line);
            XtThiSinhXetTuyen25Entity entity = parseCsvRowToEntity(columns, headerIndex);

            if (entity == null) {
                result.addError("Thiếu CCCD tại dòng " + (rowIndex + 1) + " file " + path);
                continue;
            }

            candidates.add(entity);
        }

        saveImportedCandidates(candidates, result);
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private XtThiSinhXetTuyen25Entity parseCsvRowToEntity(String[] columns, Map<String, Integer> headerIndex) {
        String cccd = getCsvValue(columns, headerIndex, "cccd", "cmnd", "cmt", "socccd", "socmnd");

        if (cccd == null || cccd.isBlank()) {
            return null;
        }

        XtThiSinhXetTuyen25Entity entity = new XtThiSinhXetTuyen25Entity();

        entity.setCccd(safe(cccd, 50));

        String soBaoDanh = getCsvValue(columns, headerIndex, "sbd", "sobaodanh", "so bao danh", "so_bao_danh");
        entity.setSoBaoDanh(safe(defaultIfBlank(soBaoDanh, generateSoBaoDanh(cccd)), 50));

        String ho = getCsvValue(columns, headerIndex, "ho", "họ");
        String ten = getCsvValue(columns, headerIndex, "ten", "tên");
        String hoTen = getCsvValue(columns, headerIndex, "hoten", "ho ten", "ho_ten", "họ tên", "hovaten", "ho va ten");

        applyName(entity, ho, ten, hoTen);

        entity.setNgaySinh(safe(getCsvValue(columns, headerIndex, "ngaysinh", "ngay sinh", "ngay_sinh", "ngày sinh"), 50));

        String dienThoai = normalizePhone(
                getCsvValue(columns, headerIndex, "dienthoai", "dien thoai", "dien_thoai", "điện thoại", "phone", "mobile")
        );
        entity.setDienThoai(safe(defaultIfBlank(dienThoai, generatePhone(cccd)), 20));

        entity.setGioiTinh(safe(getCsvValue(columns, headerIndex, "gioitinh", "gioi tinh", "gioi_tinh", "giới tính"), 20));

        String email = getCsvValue(columns, headerIndex, "email", "e-mail", "mail", "gmail");
        entity.setEmail(safe(defaultIfBlank(email, generateEmail(cccd)), 100));

        entity.setNoiSinh(safe(getCsvValue(columns, headerIndex, "noisinh", "noi sinh", "noi_sinh", "nơi sinh"), 45));
        entity.setDoiTuong(safe(getCsvValue(columns, headerIndex, "doituong", "ĐTƯT", "doi tuong", "doi_tuong", "đối tượng", "dtut"), 45));
        entity.setKhuVuc(safe(getCsvValue(columns, headerIndex, "khuvuc", "khu vuc", "khu_vuc", "khu vực", "kvut", "KVƯT"), 45));
        entity.setPassword(safe(getCsvValue(columns, headerIndex, "password", "matkhau", "mat khau", "mật khẩu"), 100));
        entity.setUpdatedAt(LocalDate.now());

        return entity;
    }

    private String getCsvValue(String[] columns, Map<String, Integer> headerIndex, String... keys) {
        for (String key : keys) {
            Integer index = headerIndex.get(normalizeHeader(key));

            if (index != null && index < columns.length) {
                return clean(columns[index]);
            }
        }

        return "";
    }

    private void importExcel(Path path, CandidateImportResult result) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            List<XtThiSinhXetTuyen25Entity> candidates = importExcelCandidates(inputStream);

            System.out.println("Đọc được từ Excel: " + candidates.size() + " dòng");

            saveImportedCandidates(candidates, result);
        }
    }

    public List<XtThiSinhXetTuyen25Entity> importExcelCandidates(InputStream inputStream) throws IOException {
        List<XtThiSinhXetTuyen25Entity> candidates = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new IOException("Không tìm thấy sheet trong file Excel");
            }

            DataFormatter formatter = new DataFormatter();

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();

            if (lastRow <= firstRow) {
                return candidates;
            }

            Row headerRow = sheet.getRow(firstRow);

            if (headerRow == null) {
                throw new IOException("Không tìm thấy hàng header trong file Excel");
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter);

            for (int rowIndex = firstRow + 1; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                XtThiSinhXetTuyen25Entity entity = parseExcelRowToEntity(row, headerIndex, formatter);

                if (entity != null) {
                    candidates.add(entity);
                }
            }
        }

        return candidates;
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headerIndex = new HashMap<>();

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i);
            String rawHeader = formatter.formatCellValue(cell);
            String normalized = normalizeHeader(rawHeader);

            if (!normalized.isBlank()) {
                headerIndex.put(normalized, i);
            }
        }

        return headerIndex;
    }

    private XtThiSinhXetTuyen25Entity parseExcelRowToEntity(
            Row row,
            Map<String, Integer> headerIndex,
            DataFormatter formatter
    ) {
        String cccd = getExcelValueByHeader(
                row,
                headerIndex,
                formatter,
                "cccd",
                "cmnd",
                "cmt",
                "socccd",
                "socmnd"
        );

        if (cccd == null || cccd.isBlank()) {
            return null;
        }

        XtThiSinhXetTuyen25Entity entity = new XtThiSinhXetTuyen25Entity();

        entity.setCccd(safe(cccd, 50));

        String soBaoDanh = getExcelValueByHeader(
                row,
                headerIndex,
                formatter,
                "sbd",
                "số báo danh",
                "so bao danh",
                "sobaodanh",
                "so_bao_danh"
        );

        entity.setSoBaoDanh(safe(defaultIfBlank(soBaoDanh, generateSoBaoDanh(cccd)), 50));

        String ho = getExcelValueByHeader(row, headerIndex, formatter, "họ", "ho");
        String ten = getExcelValueByHeader(row, headerIndex, formatter, "tên", "ten");
        String hoTen = getExcelValueByHeader(
                row,
                headerIndex,
                formatter,
                "họ tên",
                "ho ten",
                "ho_ten",
                "hoten",
                "họ và tên",
                "ho va ten",
                "hovaten",
                "fullname"
        );

        applyName(entity, ho, ten, hoTen);

        entity.setNgaySinh(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "ngày sinh",
                                "ngay sinh",
                                "ngay_sinh",
                                "ngaysinh",
                                "dateofbirth"
                        ),
                        50
                )
        );

        String dienThoai = normalizePhone(
                getExcelValueByHeader(
                        row,
                        headerIndex,
                        formatter,
                        "điện thoại",
                        "dien thoai",
                        "dien_thoai",
                        "dienthoai",
                        "số điện thoại",
                        "so dien thoai",
                        "sodienthoai",
                        "phone",
                        "mobile"
                )
        );

        entity.setDienThoai(safe(defaultIfBlank(dienThoai, generatePhone(cccd)), 20));

        entity.setGioiTinh(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "giới tính",
                                "gioi tinh",
                                "gioi_tinh",
                                "gioitinh",
                                "gender",
                                "sex"
                        ),
                        20
                )
        );

        String email = getExcelValueByHeader(
                row,
                headerIndex,
                formatter,
                "email",
                "e-mail",
                "mail",
                "gmail"
        );

        entity.setEmail(safe(defaultIfBlank(email, generateEmail(cccd)), 100));

        entity.setNoiSinh(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "nơi sinh",
                                "noi sinh",
                                "noi_sinh",
                                "noisinh",
                                "birthplace"
                        ),
                        45
                )
        );

        entity.setDoiTuong(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "đối tượng",
                                "doi tuong",
                                "doi_tuong",
                                "doituong",
                                "dtut",
                                "ĐTƯT",
                                "doi tuong uu tien",
                                "doituonguutien"
                        ),
                        45
                )
        );

        entity.setKhuVuc(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "khu vực",
                                "khu vuc",
                                "khu_vuc",
                                "khuvuc",
                                "kvut",
                                "KVƯT",
                                "khu vuc uu tien",
                                "khuvucuutien"
                        ),
                        45
                )
        );

        entity.setPassword(
                safe(
                        getExcelValueByHeader(
                                row,
                                headerIndex,
                                formatter,
                                "password",
                                "mật khẩu",
                                "mat khau",
                                "matkhau",
                                "pass"
                        ),
                        100
                )
        );

        entity.setUpdatedAt(LocalDate.now());

        return entity;
    }

    private String getExcelValueByHeader(
            Row row,
            Map<String, Integer> headerIndex,
            DataFormatter formatter,
            String... keys
    ) {
        for (String key : keys) {
            Integer index = headerIndex.get(normalizeHeader(key));

            if (index != null) {
                Cell cell = row.getCell(index);
                return clean(formatter.formatCellValue(cell));
            }
        }

        return "";
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

    private void saveImportedCandidates(
        List<XtThiSinhXetTuyen25Entity> candidates,
        CandidateImportResult result
) {
    final int QUERY_BATCH_SIZE = 1000;
    final int SAVE_BATCH_SIZE = 1000;

    System.out.println("Chuẩn bị lưu DB: " + candidates.size() + " dòng");

    List<String> cccdList = candidates.stream()
            .map(XtThiSinhXetTuyen25Entity::getCccd)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .distinct()
            .collect(Collectors.toList());

    Map<String, XtThiSinhXetTuyen25Entity> existingMap = new HashMap<>();

    for (int i = 0; i < cccdList.size(); i += QUERY_BATCH_SIZE) {
        int end = Math.min(i + QUERY_BATCH_SIZE, cccdList.size());
        List<String> batchKeys = cccdList.subList(i, end);

        List<XtThiSinhXetTuyen25Entity> existingList =
                candidateRepository.findByCccdIn(batchKeys);

        for (XtThiSinhXetTuyen25Entity entity : existingList) {
            existingMap.put(entity.getCccd(), entity);
        }
    }

    List<XtThiSinhXetTuyen25Entity> toSave = new ArrayList<>();

    for (XtThiSinhXetTuyen25Entity imported : candidates) {
        String cccd = safe(imported.getCccd(), 50);

        if (cccd == null || cccd.isBlank()) {
            result.addError("Bỏ qua dòng thiếu CCCD");
            continue;
        }

        XtThiSinhXetTuyen25Entity entity = existingMap.get(cccd);
        boolean isUpdate = entity != null;

        if (!isUpdate) {
            entity = new XtThiSinhXetTuyen25Entity();
        }

        entity.setCccd(safe(imported.getCccd(), 50));
        entity.setSoBaoDanh(safe(defaultIfBlank(imported.getSoBaoDanh(), generateSoBaoDanh(cccd)), 50));
        entity.setHo(safe(imported.getHo(), 100));
        entity.setTen(safe(imported.getTen(), 50));
        entity.setNgaySinh(safe(imported.getNgaySinh(), 50));
        entity.setDienThoai(safe(defaultIfBlank(imported.getDienThoai(), generatePhone(cccd)), 20));
        entity.setGioiTinh(safe(imported.getGioiTinh(), 20));
        entity.setEmail(safe(defaultIfBlank(imported.getEmail(), generateEmail(cccd)), 100));
        entity.setNoiSinh(safe(imported.getNoiSinh(), 45));
        entity.setDoiTuong(safe(imported.getDoiTuong(), 45));
        entity.setKhuVuc(safe(imported.getKhuVuc(), 45));
        entity.setPassword(safe(imported.getPassword(), 100));
        entity.setUpdatedAt(LocalDate.now());

        toSave.add(entity);

        if (isUpdate) {
            result.incrementUpdatedCount();
        } else {
            result.incrementImportedCount();
        }
    }

    System.out.println("Số dòng sẽ save DB: " + toSave.size());

    for (int i = 0; i < toSave.size(); i += SAVE_BATCH_SIZE) {
        int end = Math.min(i + SAVE_BATCH_SIZE, toSave.size());
        List<XtThiSinhXetTuyen25Entity> batch = toSave.subList(i, end);

        try {
            candidateRepository.saveAll(batch);
            candidateRepository.flush();
        } catch (Exception ex) {
            System.out.println("Lỗi saveAll batch: " + ex.getMessage());
            System.out.println("Chuyển sang save từng dòng cho batch lỗi...");

            for (XtThiSinhXetTuyen25Entity entity : batch) {
                try {
                    candidateRepository.save(entity);
                } catch (Exception rowEx) {
                    String error = "Lỗi lưu CCCD="
                            + entity.getCccd()
                            + " | "
                            + rowEx.getMessage();

                    System.out.println(error);
                    result.addError(error);
                }
            }
        }
    }
}

public void saveBatch(List<XtThiSinhXetTuyen25Entity> entities) {
    CandidateImportResult tempResult = new CandidateImportResult();
    saveImportedCandidates(entities, tempResult);

    if (tempResult.hasErrors()) {
        System.out.println("Có lỗi khi saveBatch:");
        for (String error : tempResult.getErrors()) {
            System.out.println(error);
        }
    }
}

    

    public void importExcelAndSave(InputStream inputStream) throws IOException {
        List<XtThiSinhXetTuyen25Entity> candidates = importExcelCandidates(inputStream);

        CandidateImportResult result = new CandidateImportResult();
        saveImportedCandidates(candidates, result);

        if (result.hasErrors()) {
            System.out.println("Có lỗi khi importExcelAndSave:");
            for (String error : result.getErrors()) {
                System.out.println(error);
            }
        }
    }

    private void applyName(
            XtThiSinhXetTuyen25Entity entity,
            String ho,
            String ten,
            String hoTen
    ) {
        ho = clean(ho);
        ten = clean(ten);
        hoTen = clean(hoTen);

        if ((ho == null || ho.isBlank() || ten == null || ten.isBlank())
                && hoTen != null
                && !hoTen.isBlank()) {

            String[] parts = parseHoTen(hoTen);
            entity.setHo(safe(parts[0], 100));
            entity.setTen(safe(parts[1], 50));
        } else {
            entity.setHo(safe(ho, 100));
            entity.setTen(safe(ten, 50));
        }
    }

    private String[] parseHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        String cleaned = hoTen.trim().replaceAll("\\s+", " ");

        if (cleaned.matches("(?i)^TS_\\S+$")) {
            String[] raw = cleaned.split("_", 2);
            return new String[]{raw[0], raw[1]};
        }

        String[] parts = cleaned.split("\\s+");

        if (parts.length == 1) {
            return new String[]{"", parts[0]};
        }

        String ten = parts[parts.length - 1];
        String ho = String.join(" ", Arrays.copyOf(parts, parts.length - 1));

        return new String[]{ho, ten};
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String cleaned = phone.replaceAll("[^0-9]", "");

        if (cleaned.isEmpty()) {
            return null;
        }

        if (!cleaned.startsWith("0")) {
            cleaned = "0" + cleaned;
        }

        return cleaned;
    }

    private String generateSoBaoDanh(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return null;
        }

        String cleaned = cccd.trim().toUpperCase();

        if (cleaned.matches("^TS_\\d+$")) {
            return cleaned;
        }

        String digits = cleaned.replaceAll("[^0-9]", "");

        if (!digits.isBlank()) {
            return "TS_" + String.format("%04d", Long.parseLong(digits));
        }

        return cleaned;
    }

    private String generateEmail(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return null;
        }

        String username = cccd.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        return username + "@admissions.local";
    }

    private String generatePhone(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return null;
        }

        String digits = cccd.replaceAll("[^0-9]", "");

        if (digits.isBlank()) {
            digits = String.valueOf(Math.abs(cccd.hashCode()));
        }

        if (digits.length() > 9) {
            digits = digits.substring(digits.length() - 9);
        }

        return "0" + String.format("%09d", Long.parseLong(digits));
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
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

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String normalizeStatValue(String value) {
        if (value == null || value.isBlank()) {
            return "Chua cap nhat";
        }
        return value.trim();
    }
}
