package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NguyenVongConsoleController {

    private final NguyenVongXetTuyenService nguyenVongService;

    public NguyenVongConsoleController(NguyenVongXetTuyenService nguyenVongService) {
        this.nguyenVongService = nguyenVongService;
    }

    public List<NguyenVongXetTuyen> loadAll() {
        return nguyenVongService.getAll();
    }

    public List<NguyenVongXetTuyen> loadByCccd(String nnCccd) {
        return nguyenVongService.getByNnCccd(nnCccd);
    }

    public NguyenVongXetTuyen save(String nnCccd,
                                   String maNganh,
                                   String maToHop,
                                   String nvThuTu,
                                   String diemThxt,
                                   String diemUtqd) {
        NguyenVongXetTuyen created = nguyenVongService.createNguyenVong(
                nnCccd.trim(),
                maNganh.trim(),
                maToHop == null ? "" : maToHop.trim(),
                Integer.parseInt(nvThuTu.trim())
        );
        return nguyenVongService.calculateScore(
                created.getId(),
                parseDecimal(diemThxt),
                parseDecimal(diemUtqd)
        );
    }

        public NguyenVongXetTuyen update(Integer id,
                         String nnCccd,
                         String maNganh,
                         String maToHop,
                         String nvThuTu,
                         String diemThxt,
                         String diemUtqd) {
        return nguyenVongService.updateNguyenVong(
            id,
            nnCccd.trim(),
            maNganh.trim(),
            maToHop == null ? "" : maToHop.trim(),
            Integer.parseInt(nvThuTu.trim()),
            parseDecimal(diemThxt),
            parseDecimal(diemUtqd)
        );
        }

    public Map<String, Object> processMajor(String maNganh) {
        return nguyenVongService.performSelectionForMajor(maNganh.trim());
    }

    public List<Map<String, Object>> processAll() {
        return nguyenVongService.performSelectionForAllMajors();
    }

    public void delete(Integer id) {
        nguyenVongService.deleteNguyenVong(id);
    }

    /**
     * Xóa tất cả nguyện vọng
     */
    public void deleteAll() {
        nguyenVongService.deleteAll();
    }

    public int importExcelFile(File file) throws Exception {
        int imported = 0;
        try (FileInputStream input = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return 0;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                return 0;
            }

            Map<String, Integer> headerMap = new HashMap<>();
            for (int c = 0; c <= header.getLastCellNum(); c++) {
                String name = getCellString(header, c, new DataFormatter());
                if (name == null || name.isEmpty()) {
                    continue;
                }
                headerMap.put(normalizeHeader(name), c);
            }

            int idxCccd = findColumnIndex(headerMap, new String[] { "nn_cccd", "cccd" });
            int idxMaNganh = findColumnIndex(headerMap, new String[] { "nv_manganh", "ma_nganh", "manganh" });
            int idxMaToHop = findColumnIndex(headerMap, new String[] { "matohop", "ma_to_hop" });
            int idxNvThuTu = findColumnIndex(headerMap, new String[] { "nv_tt", "nv_thu_tu", "nv_thutu" });
            int idxDiemThxt = findColumnIndex(headerMap, new String[] { "diem_thxt", "diemthi", "diem_thi" });
            int idxDiemUtqd = findColumnIndex(headerMap, new String[] { "diem_utqd", "diemuutien", "diem_uu_tien" });

            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String nnCccd = getCellString(row, idxCccd, formatter).trim();
                String maNganh = getCellString(row, idxMaNganh, formatter).trim();
                String maToHop = getCellString(row, idxMaToHop, formatter).trim();
                String nvThuTuText = getCellString(row, idxNvThuTu, formatter).trim();
                String diemThxtText = getCellString(row, idxDiemThxt, formatter).trim();
                String diemUtqdText = getCellString(row, idxDiemUtqd, formatter).trim();

                if (nnCccd.isEmpty() || maNganh.isEmpty() || nvThuTuText.isEmpty()) {
                    continue;
                }

                try {
                    Integer nvThuTu = Integer.parseInt(nvThuTuText);
                    NguyenVongXetTuyen created = nguyenVongService.createNguyenVong(nnCccd, maNganh, maToHop, nvThuTu);
                    nguyenVongService.calculateScore(created.getId(), parseDecimal(diemThxtText), parseDecimal(diemUtqdText));
                    imported++;
                } catch (Exception ex) {
                    // skip invalid row
                }
            }
        }
        return imported;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.toLowerCase().replace(" ", "").replace("_", "").replace("-", "");
    }

    private int findColumnIndex(Map<String, Integer> headerMap, String[] keys) {
        for (String key : keys) {
            String normalized = normalizeHeader(key);
            if (headerMap.containsKey(normalized)) {
                return headerMap.get(normalized);
            }
        }
        return -1;
    }

    private String getCellString(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0) {
            return "";
        }
        try {
            return formatter.formatCellValue(row.getCell(index));
        } catch (Exception ex) {
            return "";
        }
    }
}
