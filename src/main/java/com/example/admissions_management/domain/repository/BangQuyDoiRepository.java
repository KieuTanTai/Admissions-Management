package com.example.admissions_management.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.admissions_management.domain.model.BangQuyDoi;

public interface BangQuyDoiRepository {

    Optional<BangQuyDoi> timQuyTacChinhXac(String phuongThuc, String mon, BigDecimal diemGoc);

    Optional<BangQuyDoi> timQuyTacTheoKhoang(String phuongThuc, BigDecimal diemGoc);

    BangQuyDoi add(BangQuyDoi quyTac);

    BangQuyDoi update(BangQuyDoi quyTac);

    List<BangQuyDoi> getAll();

    Optional<BangQuyDoi> findById(Integer id);

    void delete(Integer id);

    List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi);
}