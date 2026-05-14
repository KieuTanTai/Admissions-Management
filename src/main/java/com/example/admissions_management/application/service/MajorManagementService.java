package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import com.example.admissions_management.presentation.form.model.MajorForm;
import com.example.admissions_management.presentation.form.model.MajorImportResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MajorManagementService {

    private static final int PAGE_SIZE = 20;

    private final MajorRepository majorRepository;

    public MajorManagementService(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    public List<XtNganhEntity> searchMajors(String query, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "maNganh")
        );

        Page<XtNganhEntity> results;

        if (query == null || query.isBlank()) {
            results = majorRepository.findAll(pageable);
        } else {
            results = majorRepository.findByMaNganhContainingIgnoreCaseOrTenNganhContainingIgnoreCase(
                    query.trim(),
                    query.trim(),
                    pageable
            );
        }

        return results.get().collect(Collectors.toList());
    }

    public Optional<XtNganhEntity> getMajorById(Integer id) {
        return majorRepository.findById(id);
    }

    public void saveMajor(MajorForm form) {
        XtNganhEntity entity;

        if (form.getIdnganh() != null) {
            entity = majorRepository.findById(form.getIdnganh())
                    .orElse(new XtNganhEntity());
        } else if (form.getManganh() != null && !form.getManganh().isBlank()) {
            entity = majorRepository.findByMaNganh(form.getManganh().trim())
                    .orElse(new XtNganhEntity());
        } else {
            entity = new XtNganhEntity();
        }

        entity.setMaNganh(safe(form.getManganh(), 45));
        entity.setTenNganh(safe(defaultIfBlank(form.getTennganh(), "Chưa có tên ngành"), 100));
        entity.setToHopGoc(safe(form.getnTohopgoc(), 3));
        entity.setChiTieu(form.getnChitieu() == null ? 0 : form.getnChitieu());
        entity.setDiemSan(form.getnDiemsan());
        entity.setDiemTrungTuyen(form.getnDiemtrungtuyen());
        entity.setTuyenThang(safe(form.getnTuyenthang(), 1));
        entity.setDgnl(safe(form.getnDgnl(), 1));
        entity.setThpt(safe(form.getnThpt(), 1));
        entity.setVsat(safe(form.getnVsat(), 1));
        entity.setSlXtt(form.getSlXtt());
        entity.setSlDgnl(form.getSlDgnl());
        entity.setSlVsat(form.getSlVsat());
        entity.setSlThpt(safe(form.getSlThpt(), 45));

        majorRepository.save(entity);
    }

    public void deleteMajor(Integer id) {
        majorRepository.deleteById(id);
    }

    public void deleteAllMajors() {
        majorRepository.deleteAll();
    }

    public MajorImportResult importExcelFile(Path path) {
        MajorImportResult result = new MajorImportResult();

        try (InputStream inputStream = Files.newInputStream(path)) {
            List<XtNganhEntity> majors = readExcel(inputStream);

            if (majors.isEmpty()) {
                result.addError("File không đọc được dòng ngành nào. Kiểm tra lại header.");
                return result;
            }

            saveImportedMajors(majors, result);
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

    private List<XtNganhEntity> readExcel(InputStream inputStream) throws IOException {
        List<XtNganhEntity> majors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new IOException("Không tìm thấy sheet.");
            }

            DataFormatter formatter = new DataFormatter();

            int headerRowIndex = findHeaderRow(sheet, formatter);

            if (headerRowIndex < 0) {
                throw new IOException("Không tìm thấy dòng header có cột mã ngành.");
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter);

            System.out.println("HEADER ROW INDEX = " + headerRowIndex);
            System.out.println("HEADER MAP = " + headerIndex);

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                XtNganhEntity entity = parseRow(row, headerIndex, formatter);

                if (entity != null) {
                    majors.add(entity);
                }
            }
        }

        System.out.println("Tổng số dòng đọc được từ Excel = " + majors.size());

        return majors;
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
                    "manganh",
                    "ma nganh",
                    "mã ngành",
                    "mactdt",
                    "ma ctdt",
                    "mã ctđt",
                    "maxettuyen",
                    "ma xet tuyen",
                    "mã xét tuyển",
                    "manganhtuyensinh",
                    "manganhxt"
            )) {
                return i;
            }
        }
        return -1;
    }

    private XtNganhEntity parseRow(Row row, Map<String, Integer> headerIndex, DataFormatter formatter) {
        String maNganh = getValue(row, headerIndex, formatter,
            "manganh",
            "ma nganh",
            "mã ngành",
            "ma_nganh",
            "mactdt",
            "ma ctdt",
            "mã ctđt",
            "maxettuyen",
            "ma xet tuyen",
            "mã xét tuyển",
            "manganhtuyensinh",
            "manganhxt",
            "manganhdaotao"
        );

        if (maNganh == null || maNganh.isBlank()) {
            return null;
        }

        String tenNganh = getValue(row, headerIndex, formatter,
            "tennganh",
            "ten nganh",
            "tên ngành",
            "ten_nganh",
            "tenctdt",
            "ten ctdt",
            "tên ctđt",
            "tennganhchuan",
            "ten_nganhchuan",
            "ten nganh chuan",
            "tên ngành chuẩn",
            "tenchuongtrinh",
            "ten chuong trinh",
            "tên chương trình",
            "tenchuongtrinhdaotao"
        );

        XtNganhEntity entity = new XtNganhEntity();

        entity.setMaNganh(safe(maNganh, 45));
        entity.setTenNganh(safe(defaultIfBlank(tenNganh, "Chưa có tên ngành"), 100));

        String toHopGoc = getValue(row, headerIndex, formatter,
                "n_tohopgoc",
                "tohopgoc",
                "to hop goc",
                "tổ hợp gốc",
                "goc",
                "gốc"
        );

        String maToHop = getValue(row, headerIndex, formatter,
                "matohop",
                "ma to hop",
                "mã tổ hợp",
                "ma_to_hop"
        );

        String tenToHop = getValue(row, headerIndex, formatter,
                "tentohop",
                "ten to hop",
                "tên tổ hợp",
                "ten_to_hop"
        );

        if ("goc".equalsIgnoreCase(normalizeHeader(toHopGoc))) {
            entity.setToHopGoc(safe(defaultIfBlank(tenToHop, extractToHopCode(maToHop)), 3));
        } else {
            entity.setToHopGoc(safe(toHopGoc, 3));
        }

        Integer chiTieu = toInteger(getValue(row, headerIndex, formatter,
                "n_chitieu",
                "chitieu",
                "chi tieu",
                "chỉ tiêu",
                "chitieuchot",
                "chi tieu chot",
                "chỉ tiêu chốt"
        ));

        entity.setChiTieu(chiTieu == null ? 0 : chiTieu);

        entity.setDiemSan(toBigDecimal(getValue(row, headerIndex, formatter,
            "n_diemsan",
            "diemsan",
            "diem san",
            "điểm sàn",
            "nguongdauvao",
            "nguong dau vao",
            "ngưỡng đầu vào"
     )));

        entity.setDiemTrungTuyen(toBigDecimal(getValue(row, headerIndex, formatter,
                "n_diemtrungtuyen",
                "diemtrungtuyen",
                "diem trung tuyen",
                "điểm trúng tuyển"
        )));

        entity.setTuyenThang(safe(getValue(row, headerIndex, formatter,
                "n_tuyenthang",
                "tuyenthang",
                "tuyen thang",
                "tuyển thẳng"
        ), 1));

        entity.setDgnl(safe(getValue(row, headerIndex, formatter,
                "n_dgnl",
                "dgnl",
                "đgnl"
        ), 1));

        entity.setThpt(safe(getValue(row, headerIndex, formatter,
                "n_thpt",
                "thpt"
        ), 1));

        entity.setVsat(safe(getValue(row, headerIndex, formatter,
                "n_vsat",
                "vsat",
                "v-sat"
        ), 1));

        entity.setSlXtt(toInteger(getValue(row, headerIndex, formatter,
                "sl_xtt",
                "so luong xtt",
                "số lượng xtt"
        )));

        entity.setSlDgnl(toInteger(getValue(row, headerIndex, formatter,
                "sl_dgnl",
                "so luong dgnl",
                "số lượng dgnl"
        )));

        entity.setSlVsat(toInteger(getValue(row, headerIndex, formatter,
                "sl_vsat",
                "so luong vsat",
                "số lượng vsat"
        )));

        entity.setSlThpt(safe(getValue(row, headerIndex, formatter,
                "sl_thpt",
                "so luong thpt",
                "số lượng thpt"
        ), 45));

        return entity;
    }

   private void saveImportedMajors(List<XtNganhEntity> importedList, MajorImportResult result) {
        Map<String, XtNganhEntity> importMap = new LinkedHashMap<>();

        for (XtNganhEntity imported : importedList) {
            String maNganh = safe(imported.getMaNganh(), 45);

            if (maNganh == null || maNganh.isBlank()) {
                result.addError("Bỏ qua dòng thiếu mã ngành.");
                continue;
            }

            XtNganhEntity current = importMap.get(maNganh);

            if (current == null) {
                importMap.put(maNganh, imported);
            } else {
                mergeMajor(current, imported);
            }
        }

        List<String> maNganhList = new ArrayList<>(importMap.keySet());

        Map<String, XtNganhEntity> existingMap = new HashMap<>();
        List<XtNganhEntity> existingList = majorRepository.findByMaNganhIn(maNganhList);

        for (XtNganhEntity entity : existingList) {
            existingMap.put(entity.getMaNganh(), entity);
        }

        List<XtNganhEntity> toSave = new ArrayList<>();

        for (Map.Entry<String, XtNganhEntity> entry : importMap.entrySet()) {
            String maNganh = entry.getKey();
            XtNganhEntity imported = entry.getValue();

            XtNganhEntity entity = existingMap.get(maNganh);
            boolean isUpdate = entity != null;

            if (!isUpdate) {
                entity = new XtNganhEntity();
            }

            entity.setMaNganh(imported.getMaNganh());

            // Set tên ngành, kiểm tra nếu tên ngành thiếu thì giữ tên cũ
            if (imported.getTenNganh() != null && !imported.getTenNganh().equalsIgnoreCase("Chưa có tên ngành")) {
                entity.setTenNganh(imported.getTenNganh());
            } else {
                if (entity.getTenNganh() == null || entity.getTenNganh().equalsIgnoreCase("Chưa có tên ngành")) {
                    entity.setTenNganh("Chưa có tên ngành");
                }
            }

            if (imported.getToHopGoc() != null && !imported.getToHopGoc().isBlank()) {
                entity.setToHopGoc(imported.getToHopGoc());
            }

            if (imported.getChiTieu() != null && imported.getChiTieu() > 0) {
                entity.setChiTieu(imported.getChiTieu());
            } else if (entity.getChiTieu() == null) {
                entity.setChiTieu(0);
            }

            if (imported.getDiemSan() != null) {
                entity.setDiemSan(imported.getDiemSan());
            }

            if (imported.getDiemTrungTuyen() != null) {
                entity.setDiemTrungTuyen(imported.getDiemTrungTuyen());
            }

            if (imported.getTuyenThang() != null) {
                entity.setTuyenThang(imported.getTuyenThang());
            }

            if (imported.getDgnl() != null) {
                entity.setDgnl(imported.getDgnl());
            }

            if (imported.getThpt() != null) {
                entity.setThpt(imported.getThpt());
            }

            if (imported.getVsat() != null) {
                entity.setVsat(imported.getVsat());
            }

            if (imported.getSlXtt() != null) {
                entity.setSlXtt(imported.getSlXtt());
            }

            if (imported.getSlDgnl() != null) {
                entity.setSlDgnl(imported.getSlDgnl());
            }

            if (imported.getSlVsat() != null) {
                entity.setSlVsat(imported.getSlVsat());
            }

            if (imported.getSlThpt() != null) {
                entity.setSlThpt(imported.getSlThpt());
            }

            toSave.add(entity);

            if (isUpdate) {
                result.incrementUpdatedCount();
            } else {
                result.incrementImportedCount();
            }
        }

        if (!toSave.isEmpty()) {
            majorRepository.saveAll(toSave);
            majorRepository.flush();
        }
    }

    private void mergeMajor(XtNganhEntity current, XtNganhEntity imported) {
        if (isNotBlank(imported.getTenNganh())) {
            current.setTenNganh(imported.getTenNganh());
        }

        if (isNotBlank(imported.getToHopGoc())) {
            current.setToHopGoc(imported.getToHopGoc());
        }

        if (imported.getChiTieu() != null && imported.getChiTieu() > 0) {
            current.setChiTieu(imported.getChiTieu());
        }

        if (imported.getDiemSan() != null) {
            current.setDiemSan(imported.getDiemSan());
        }

        if (imported.getDiemTrungTuyen() != null) {
            current.setDiemTrungTuyen(imported.getDiemTrungTuyen());
        }

        if (isNotBlank(imported.getTuyenThang())) {
            current.setTuyenThang(imported.getTuyenThang());
        }

        if (isNotBlank(imported.getDgnl())) {
            current.setDgnl(imported.getDgnl());
        }

        if (isNotBlank(imported.getThpt())) {
            current.setThpt(imported.getThpt());
        }

        if (isNotBlank(imported.getVsat())) {
            current.setVsat(imported.getVsat());
        }

        if (imported.getSlXtt() != null) {
            current.setSlXtt(imported.getSlXtt());
        }

        if (imported.getSlDgnl() != null) {
            current.setSlDgnl(imported.getSlDgnl());
        }

        if (imported.getSlVsat() != null) {
            current.setSlVsat(imported.getSlVsat());
        }

        if (isNotBlank(imported.getSlThpt())) {
            current.setSlThpt(imported.getSlThpt());
        }
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
                System.out.println("HEADER RAW = " + header + " | NORMALIZED = " + normalized);
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

    private Integer toInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }

            value = value.replace(",", "")
                    .replace(".", "")
                    .trim();

            return new BigDecimal(value).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }

            value = value.replace(",", ".").trim();

            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
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

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}