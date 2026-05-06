package com.example.admissions_management.presentation.form.view;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                BangQuyDoi qd = new BangQuyDoi();
                
                // Các lệnh set dữ liệu của bạn giữ nguyên...
                qd.setPhuongThuc(getStringValue(row.getCell(0)));
                qd.setToHop(getStringValue(row.getCell(1)));
                qd.setMon(getStringValue(row.getCell(2)));
                qd.setDiemA(getBigDecimalValue(row.getCell(3)));
                qd.setDiemB(getBigDecimalValue(row.getCell(4)));
                qd.setDiemC(getBigDecimalValue(row.getCell(5)));
                qd.setDiemD(getBigDecimalValue(row.getCell(6)));
                qd.setMaQuyDoi(getStringValue(row.getCell(7)));
                qd.setPhanVi(getStringValue(row.getCell(8)));

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