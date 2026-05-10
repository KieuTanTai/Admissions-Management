package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.dto.request.DiemCongImportRequest;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.application.service.DiemCongXetTuyenService;
import com.example.admissions_management.application.service.ExcelService;
import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

@Component
public class DiemCongConsoleController {

    private final DiemCongXetTuyenService diemCongService;
    private final ExcelService excelService;

    public DiemCongConsoleController(DiemCongXetTuyenService diemCongService, ExcelService excelService) {
        this.diemCongService = diemCongService;
        this.excelService = excelService;
    }

    public List<DiemCongXetTuyen> loadAll() {
        return diemCongService.getAll();
    }

    public List<DiemCongXetTuyen> loadByCccd(String tsCccd) {
        return diemCongService.findByTsCccd(tsCccd);
    }

    public DiemCongXetTuyen save(String tsCccd,
                                 String maNganh,
                                 String maToHop,
                                 String phuongThuc,
                                 String diemCc,
                                 String diemUtxt,
                                 String diemTong,
                                 String ghiChu) {
        DiemCongImportRequest request = new DiemCongImportRequest(
                tsCccd.trim(),
                maNganh.trim(),
                maToHop == null ? "" : maToHop.trim(),
                phuongThuc == null ? "" : phuongThuc.trim(),
                parseDecimal(diemCc),
                parseDecimal(diemUtxt),
                parseDecimal(diemTong),
                ghiChu == null ? "" : ghiChu.trim()
        );
        return diemCongService.saveOrUpdate(request);
    }

    public void delete(Long id) {
        diemCongService.delete(id);
    }

    /**
     * Xóa tất cả dữ liệu điểm cộng
     */
    public void deleteAll() {
        diemCongService.deleteAll();
    }

    /**
     * Import dữ liệu điểm cộng từ file Excel
     */
    public DiemCongImportSummary importExcelFile(File file) throws Exception {
        // Use batch import (1000 records per batch) for better performance
        return excelService.importDiemCongFromFileBatch(file, 1000);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
}
