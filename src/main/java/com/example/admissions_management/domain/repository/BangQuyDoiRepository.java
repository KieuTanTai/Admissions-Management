package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.BangQuyDoi;

import java.util.List;
import java.util.Optional;

public interface BangQuyDoiRepository {
    List<BangQuyDoi> findAll();
    Optional<BangQuyDoi> findById(Integer id);
    List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi);
    BangQuyDoi save(BangQuyDoi bqd);
    void delete(Integer id);
    void deleteAll();
    void saveAll(List<BangQuyDoi> items);
}
