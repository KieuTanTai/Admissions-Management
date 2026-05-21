package com.example.admissions_management.presentation.form.view;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.example.admissions_management.domain.model.BangQuyDoi;

public class ExcelHelper {

    public static List<BangQuyDoi> docFileExcel(File file) throws Exception {
        List<BangQuyDoi> danhSach = new ArrayList<>();
        
        // SỬ DỤNG FileInputStream ĐỂ ĐẢM BẢO CHỈ ĐỌC (READ-ONLY)
        // Việc dùng try-with-resources cho cả 2 sẽ đảm bảo "đọc xong là đóng ngay"
        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return danhSach;
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = buildHeaderMap(headerRow);
            boolean hasStructuredHeader = headerMap.containsKey(normalizeHeader("CMND"))
                    && headerMap.containsKey(normalizeHeader("MAMONTHI"))
                    && headerMap.containsKey(normalizeHeader("DIEM"));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                BangQuyDoi qd = new BangQuyDoi();

                if (hasStructuredHeader) {
                    // Nếu file là dữ liệu thi VSAT/DGNL dạng bảng chi tiết, chỉ lấy những cột liên quan
                    String monThi = getStringValueByHeader(row, headerMap, "MAMONTHI");
                    String tenMon = getStringValueByHeader(row, headerMap, "TENMONTHI");
                    BigDecimal diem = getBigDecimalValueByHeader(row, headerMap, "DIEM");
                    BigDecimal thangDiem = getBigDecimalValueByHeader(row, headerMap, "THANGDIEM");

                    qd.setPhuongThuc(resolveMethodFromSheet(sheet.getSheetName(), monThi, thangDiem));
                    qd.setToHop(resolveToHopFromSubject(monThi, tenMon));
                    qd.setMon(resolveMonFromSubject(monThi, tenMon));
                    qd.setDiemA(diem);
                    qd.setDiemB(thangDiem);
                    qd.setDiemC(null);
                    qd.setDiemD(null);
                    qd.setMaQuyDoi(buildMaQuyDoi(sheet.getSheetName(), monThi));
                    qd.setPhanVi(diem == null ? "" : diem.toPlainString());
                } else {
                    // Hỗ trợ template cũ: Phương Thức | Tổ Hợp | Môn | Điểm A | Điểm B | Điểm C | Điểm D | Mã Quy Đổi | Phân Vị
                    qd.setPhuongThuc(getStringValue(row.getCell(0)));
                    qd.setToHop(getStringValue(row.getCell(1)));
                    qd.setMon(getStringValue(row.getCell(2)));
                    qd.setDiemA(getBigDecimalValue(row.getCell(3)));
                    qd.setDiemB(getBigDecimalValue(row.getCell(4)));
                    qd.setDiemC(getBigDecimalValue(row.getCell(5)));
                    qd.setDiemD(getBigDecimalValue(row.getCell(6)));
                    qd.setMaQuyDoi(getStringValue(row.getCell(7)));
                    qd.setPhanVi(getStringValue(row.getCell(8)));
                }

                danhSach.add(qd);
            }
        } // Kết thúc khối try này, file sẽ được Windows mở khóa hoàn toàn
        return danhSach;
    }

    // Kiểm tra dòng trống để tránh lỗi DB
    private static boolean isRowEmpty(Row row) {
        Cell firstCell = row.getCell(0);
        return (firstCell == null || firstCell.getCellType() == CellType.BLANK);
    }

    private static Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        if (headerRow == null) {
            return headerMap;
        }

        for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            String key = normalizeHeader(getStringValue(cell));
            if (!key.isBlank()) {
                headerMap.put(key, i);
            }
        }
        return headerMap;
    }

    private static String normalizeHeader(String value) {
        if (value == null) return "";
        String normalized = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
        return normalized;
    }

    private static String getStringValueByHeader(Row row, Map<String, Integer> headerMap, String header) {
        Integer index = headerMap.get(normalizeHeader(header));
        if (index == null) return "";
        return getStringValue(row.getCell(index));
    }

    private static BigDecimal getBigDecimalValueByHeader(Row row, Map<String, Integer> headerMap, String header) {
        Integer index = headerMap.get(normalizeHeader(header));
        if (index == null) return null;
        return getBigDecimalValue(row.getCell(index));
    }

    private static String resolveMethodFromSheet(String sheetName, String monThi, BigDecimal thangDiem) {
        String normalizedSheet = normalizeHeader(sheetName);
        if (normalizedSheet.contains("DGNL")) {
            return "DGNL";
        }
        if (normalizedSheet.contains("VSAT")) {
            return "VSAT";
        }
        if (thangDiem != null && thangDiem.compareTo(new BigDecimal("150")) == 0) {
            return "VSAT";
        }
        if (monThi != null && normalizeHeader(monThi).contains("DGNL")) {
            return "DGNL";
        }
        return "VSAT";
    }

    private static String resolveToHopFromSubject(String monThi, String tenMon) {
        String normalized = normalizeHeader(monThi);
        if (normalized.contains("TO_VS") || normalized.equals("M1") || normalized.contains("TOAN")) return "TOAN";
        if (normalized.contains("LI_VS") || normalized.equals("M2") || normalized.contains("VATLY")) return "VAT_LY";
        if (normalized.contains("HO_VS") || normalized.equals("M3") || normalized.contains("HOAHOC")) return "HOA_HOC";
        if (normalized.contains("SI_VS") || normalized.equals("M4") || normalized.contains("SINHHOC")) return "SINH_HOC";
        if (normalized.contains("SU_VS") || normalized.equals("M6") || normalized.contains("LICHSU")) return "LICH_SU";
        if (normalized.contains("DI_VS") || normalized.equals("M7") || normalized.contains("DIALY")) return "DIA_LY";
        if (normalized.contains("VA_VS") || normalized.equals("M5") || normalized.contains("NGUVAN")) return "NGU_VAN";
        if (normalized.contains("N1_VS") || normalized.equals("M8") || normalized.equals("N1") || normalized.contains("TIENGANH")) return "TIENG_ANH";
        if (normalized.equals("TI") || normalized.contains("TINHOC")) return "TI";
        if (normalized.equals("CNCN") || normalized.contains("CONGNGHECONGNGHIEP")) return "CNCN";
        if (normalized.equals("CNNN") || normalized.contains("CONGNGHENONGNGHIEP")) return "CNNN";
        if (normalized.equals("KTPL") || normalized.contains("KINHTEPHAPLUAT")) return "KTPL";
        String ten = normalizeHeader(tenMon);
        if (ten.contains("TOAN")) return "TOAN";
        if (ten.contains("VATLY")) return "VAT_LY";
        if (ten.contains("HOAHOC")) return "HOA_HOC";
        if (ten.contains("SINHHOC")) return "SINH_HOC";
        if (ten.contains("LICHSU")) return "LICH_SU";
        if (ten.contains("DIALY")) return "DIA_LY";
        if (ten.contains("NGUVAN")) return "NGU_VAN";
        if (ten.contains("TIENGANH")) return "TIENG_ANH";
        if (ten.contains("TINHOC")) return "TI";
        if (ten.contains("CONGNGHECONGNGHIEP")) return "CNCN";
        if (ten.contains("CONGNGHENONGNGHIEP")) return "CNNN";
        if (ten.contains("KINHTEPHAPLUAT")) return "KTPL";
        return monThi == null ? "" : monThi.trim();
    }

    private static String resolveMonFromSubject(String monThi, String tenMon) {
        String mapped = resolveToHopFromSubject(monThi, tenMon);
        return mapped.isBlank() ? (monThi == null ? "" : monThi.trim()) : mapped;
    }

    private static String buildMaQuyDoi(String sheetName, String monThi) {
        String prefix = normalizeHeader(sheetName).contains("DGNL") ? "DGNL" : "VSAT";
        return prefix + "_" + normalizeHeader(monThi);
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            // Dùng DataFormatter để lấy giá trị hiển thị y hệt như nhìn thấy trong Excel
            return new org.apache.poi.ss.usermodel.DataFormatter().formatCellValue(cell);
        }
        return "";
    }

    private static BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return new BigDecimal(String.valueOf(cell.getNumericCellValue()));
            }
            // Quan trọng: Thay đổi dấu phẩy thành dấu chấm để BigDecimal đọc được
            String val = cell.getStringCellValue().trim().replace(",", ".");
            return new BigDecimal(val);
        } catch (Exception e) {
            return null;
        }
    }
}