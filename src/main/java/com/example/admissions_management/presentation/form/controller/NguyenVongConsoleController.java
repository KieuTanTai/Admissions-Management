package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
}
