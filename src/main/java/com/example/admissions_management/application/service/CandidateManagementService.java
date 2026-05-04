package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.presentation.web.model.CandidateForm;
import com.example.admissions_management.presentation.web.model.CandidateImportResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CandidateManagementService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CandidateRepository candidateRepository;

    public CandidateManagementService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public Page<XtThiSinhXetTuyen25Entity> searchCandidates(String query, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        if (!StringUtils.hasText(query)) {
            return candidateRepository.findAll(pageable);
        }
        String normalized = query.trim();
        return candidateRepository.findByCccdContainingIgnoreCaseOrHoContainingIgnoreCaseOrTenContainingIgnoreCase(
                normalized, normalized, normalized, pageable);
    }

    public Optional<XtThiSinhXetTuyen25Entity> findById(Integer id) {
        return candidateRepository.findById(id);
    }

    public XtThiSinhXetTuyen25Entity saveCandidate(CandidateForm form) {
        XtThiSinhXetTuyen25Entity entity;
        if (form.getId() != null) {
            entity = candidateRepository.findById(form.getId()).orElse(new XtThiSinhXetTuyen25Entity());
        } else {
            entity = candidateRepository.findByCccd(form.getCccd()).orElse(new XtThiSinhXetTuyen25Entity());
        }
        applyFormToEntity(form, entity);
        return candidateRepository.save(entity);
    }

    public void deleteCandidate(Integer id) {
        candidateRepository.deleteById(id);
    }

    public void deleteAllCandidates() {
        candidateRepository.deleteAll();
    }

    public CandidateImportResult importCandidates(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            CandidateImportResult result = new CandidateImportResult();
            result.addError("File nhap khong duoc de trong.");
            return result;
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
            return importFromExcel(file);
        } else {
            return importFromCsv(file);
        }
    }

    private CandidateImportResult importFromExcel(MultipartFile file) {
        CandidateImportResult result = new CandidateImportResult();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                result.addError("File Excel khong co du lieu.");
                return result;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addError("File Excel khong co dong header.");
                return result;
            }

            Map<String, Integer> headerIndex = parseExcelHeader(headerRow);
            if (!headerIndex.containsKey("cccd")) {
                result.addError("File Excel phai co cot 'cccd'.");
                return result;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String cccd = getExcelCell(row, headerIndex.get("cccd"));
                if (!StringUtils.hasText(cccd)) {
                    result.addError("Dong " + (i + 1) + ": CCCD khong duoc de trong.");
                    continue;
                }

                try {
                    XtThiSinhXetTuyen25Entity entity = candidateRepository.findByCccd(cccd.trim())
                            .orElse(new XtThiSinhXetTuyen25Entity());
                    applyExcelRowToEntity(row, headerIndex, entity);
                    entity.setUpdatedAt(LocalDate.now());
                    boolean existed = entity.getId() != null;
                    candidateRepository.save(entity);
                    if (existed) {
                        result.incrementUpdatedCount();
                    } else {
                        result.incrementImportedCount();
                    }
                } catch (DataIntegrityViolationException ex) {
                    result.addError("Dong " + (i + 1) + ": loi du lieu khong hop le hoac trung CCCD.");
                } catch (Exception ex) {
                    result.addError("Dong " + (i + 1) + ": loi khi xu ly du lieu - " + ex.getMessage());
                }
            }

            result.setMessage("Import hoan tat. " + result.getImportedCount() + " dong moi, "
                    + result.getUpdatedCount() + " dong cap nhat.");
        } catch (IOException ex) {
            result.addError("Khong the doc file Excel: " + ex.getMessage());
        }
        return result;
    }

    private CandidateImportResult importFromCsv(MultipartFile file) {
        CandidateImportResult result = new CandidateImportResult();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (!StringUtils.hasText(headerLine)) {
                result.addError("Tap tin khong co dong header.");
                return result;
            }

            Map<String, Integer> headerIndex = parseHeader(headerLine);
            if (!headerIndex.containsKey("cccd")) {
                result.addError("Tap tin phai co cot 'cccd'.");
                return result;
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                String[] cells = splitCsvLine(line);
                String cccd = getCell(cells, headerIndex.get("cccd"));
                if (!StringUtils.hasText(cccd)) {
                    result.addError("Dong " + row + ": CCCD khong duoc de trong.");
                    continue;
                }
                try {
                    XtThiSinhXetTuyen25Entity entity = candidateRepository.findByCccd(cccd.trim())
                            .orElse(new XtThiSinhXetTuyen25Entity());
                    applyCsvCellsToEntity(cells, headerIndex, entity);
                    entity.setUpdatedAt(LocalDate.now());
                    boolean existed = entity.getId() != null;
                    candidateRepository.save(entity);
                    if (existed) {
                        result.incrementUpdatedCount();
                    } else {
                        result.incrementImportedCount();
                    }
                } catch (DataIntegrityViolationException ex) {
                    result.addError("Dong " + row + ": loi du lieu khong hop le hoac trung CCCD.");
                } catch (Exception ex) {
                    result.addError("Dong " + row + ": loi khi xu ly du lieu - " + ex.getMessage());
                }
            }

            result.setMessage("Import hoan tat. " + result.getImportedCount() + " dong moi, "
                    + result.getUpdatedCount() + " dong cap nhat.");
        } catch (IOException ex) {
            result.addError("Khong the doc file: " + ex.getMessage());
        }
        return result;
    }

    private Map<String, Integer> parseExcelHeader(Row headerRow) {
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = getExcelCell(headerRow, i);
            if (header != null) {
                String canonical = normalizeHeader(header);
                String field = mapHeaderToField(canonical);
                if (field != null) {
                    headerIndex.put(field, i);
                }
            }
        }
        return headerIndex;
    }

    private String getExcelCell(Row row, Integer cellIndex) {
        if (cellIndex == null || cellIndex < 0 || cellIndex >= row.getLastCellNum()) {
            return null;
        }
        try {
            String value = row.getCell(cellIndex).getStringCellValue();
            return value != null ? value.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void applyExcelRowToEntity(Row row, Map<String, Integer> headerIndex,
                                       XtThiSinhXetTuyen25Entity entity) {
        entity.setCccd(trim(getExcelCell(row, headerIndex.get("cccd"))));
        entity.setSoBaoDanh(trim(getExcelCell(row, headerIndex.get("soBaoDanh"))));
        entity.setHo(trim(getExcelCell(row, headerIndex.get("ho"))));
        entity.setTen(trim(getExcelCell(row, headerIndex.get("ten"))));
        entity.setNgaySinh(trim(getExcelCell(row, headerIndex.get("ngaySinh"))));
        entity.setDienThoai(trim(getExcelCell(row, headerIndex.get("dienThoai"))));
        entity.setGioiTinh(trim(getExcelCell(row, headerIndex.get("gioiTinh"))));
        entity.setEmail(trim(getExcelCell(row, headerIndex.get("email"))));
        entity.setNoiSinh(trim(getExcelCell(row, headerIndex.get("noiSinh"))));
        entity.setDoiTuong(trim(getExcelCell(row, headerIndex.get("doiTuong"))));
        entity.setKhuVuc(trim(getExcelCell(row, headerIndex.get("khuVuc"))));
        entity.setPassword(trim(getExcelCell(row, headerIndex.get("password"))));
    }

    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> headerIndex = new HashMap<>();
        String[] headers = splitCsvLine(headerLine);
        for (int i = 0; i < headers.length; i++) {
            String canonical = normalizeHeader(headers[i]);
            String field = mapHeaderToField(canonical);
            if (field != null) {
                headerIndex.put(field, i);
            }
        }
        return headerIndex;
    }

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String getCell(String[] cells, Integer index) {
        if (index == null || index < 0 || index >= cells.length) {
            return null;
        }
        String value = cells[index].trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
    }

    private String mapHeaderToField(String normalized) {
        return switch (normalized) {
            case "cccd" -> "cccd";
            case "sobaodanh", "sobd", "sbd" -> "soBaoDanh";
            case "ho" -> "ho";
            case "ten" -> "ten";
            case "ngaysinh" -> "ngaySinh";
            case "dienthoai", "sodienthoai", "phone" -> "dienThoai";
            case "gioitinh" -> "gioiTinh";
            case "email" -> "email";
            case "noisinh" -> "noiSinh";
            case "doituong" -> "doiTuong";
            case "khuvuc" -> "khuVuc";
            case "password" -> "password";
            default -> null;
        };
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private void applyCsvCellsToEntity(String[] cells, Map<String, Integer> headerIndex,
                                       XtThiSinhXetTuyen25Entity entity) {
        entity.setCccd(trim(getCell(cells, headerIndex.get("cccd"))));
        entity.setSoBaoDanh(trim(getCell(cells, headerIndex.get("soBaoDanh"))));
        entity.setHo(trim(getCell(cells, headerIndex.get("ho"))));
        entity.setTen(trim(getCell(cells, headerIndex.get("ten"))));
        entity.setNgaySinh(trim(getCell(cells, headerIndex.get("ngaySinh"))));
        entity.setDienThoai(trim(getCell(cells, headerIndex.get("dienThoai"))));
        entity.setGioiTinh(trim(getCell(cells, headerIndex.get("gioiTinh"))));
        entity.setEmail(trim(getCell(cells, headerIndex.get("email"))));
        entity.setNoiSinh(trim(getCell(cells, headerIndex.get("noiSinh"))));
        entity.setDoiTuong(trim(getCell(cells, headerIndex.get("doiTuong"))));
        entity.setKhuVuc(trim(getCell(cells, headerIndex.get("khuVuc"))));
        entity.setPassword(trim(getCell(cells, headerIndex.get("password"))));
    }

    private void applyFormToEntity(CandidateForm form, XtThiSinhXetTuyen25Entity entity) {
        entity.setCccd(trim(form.getCccd()));
        entity.setSoBaoDanh(trim(form.getSoBaoDanh()));
        entity.setHo(trim(form.getHo()));
        entity.setTen(trim(form.getTen()));
        entity.setNgaySinh(trim(form.getNgaySinh()));
        entity.setDienThoai(trim(form.getDienThoai()));
        entity.setGioiTinh(trim(form.getGioiTinh()));
        entity.setEmail(trim(form.getEmail()));
        entity.setNoiSinh(trim(form.getNoiSinh()));
        entity.setDoiTuong(trim(form.getDoiTuong()));
        entity.setKhuVuc(trim(form.getKhuVuc()));
        entity.setPassword(trim(form.getPassword()));
        entity.setUpdatedAt(LocalDate.now());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
