package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.BangQuyDoiService;
import com.example.admissions_management.application.service.ExcelService;
import com.example.admissions_management.domain.model.BangQuyDoi;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class BangQuyDoiConsoleController {

    private final BangQuyDoiService service;
    private final ExcelService excelService;

    public BangQuyDoiConsoleController(BangQuyDoiService service, ExcelService excelService) {
        this.service = service;
        this.excelService = excelService;
    }

    public List<BangQuyDoi> loadAll() {
        return service.getAll();
    }

    public List<BangQuyDoi> loadByMaQuyDoi(String ma) {
        return service.findByMaQuyDoi(ma);
    }

    public BangQuyDoi save(String phuongThuc, String toHop, String mon,
                           String diemA, String diemB, String diemC, String diemD,
                           String maQuyDoi, String phanVi) {
        BangQuyDoi d = new BangQuyDoi();
        d.setPhuongThuc(phuongThuc == null ? "" : phuongThuc.trim());
        d.setToHop(toHop == null ? "" : toHop.trim());
        d.setMon(mon == null ? "" : mon.trim());
        try { d.setDiemA(diemA == null || diemA.isBlank() ? null : new java.math.BigDecimal(diemA.trim())); } catch (Exception e) { d.setDiemA(null); }
        try { d.setDiemB(diemB == null || diemB.isBlank() ? null : new java.math.BigDecimal(diemB.trim())); } catch (Exception e) { d.setDiemB(null); }
        try { d.setDiemC(diemC == null || diemC.isBlank() ? null : new java.math.BigDecimal(diemC.trim())); } catch (Exception e) { d.setDiemC(null); }
        try { d.setDiemD(diemD == null || diemD.isBlank() ? null : new java.math.BigDecimal(diemD.trim())); } catch (Exception e) { d.setDiemD(null); }
        d.setMaQuyDoi(maQuyDoi == null ? "" : maQuyDoi.trim());
        d.setPhanVi(phanVi == null ? "" : phanVi.trim());
        return service.saveOrUpdate(d);
    }

    public void delete(Integer id) {
        service.delete(id);
    }

    public void deleteAll() {
        service.deleteAll();
    }

    public int importExcelFile(File file) throws Exception {
        List<BangQuyDoi> parsed = excelService.importBangQuyDoiFromFile(file);
        if (parsed == null || parsed.isEmpty()) return 0;
        service.saveAll(parsed);
        return parsed.size();
    }
}
