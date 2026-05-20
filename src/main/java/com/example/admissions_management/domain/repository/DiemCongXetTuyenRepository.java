package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import java.util.List;
import java.util.Optional;

public interface DiemCongXetTuyenRepository {
    
    List<DiemCongXetTuyen> findAll();
    
    List<DiemCongXetTuyen> findByTsCccd(String tsCccd);
    
    List<DiemCongXetTuyen> findByMaNganh(String maNganh);
    
    Optional<DiemCongXetTuyen> findByDcKeys(String dcKeys);

    Optional<DiemCongXetTuyen> findById(Long id);
    
    List<DiemCongXetTuyen> findByTsCccdAndMaNganhAndMaToHop(String tsCccd, String maNganh, String maToHop);
    
    DiemCongXetTuyen save(DiemCongXetTuyen diemCong);
    
    DiemCongXetTuyen update(DiemCongXetTuyen diemCong);
    
    void delete(Long id);

    List<DiemCongXetTuyen> findByDcKeysIn(List<String> dcKeys);

    void saveAll(List<DiemCongXetTuyen> diemCongs);
    void bulkUpsert(List<DiemCongXetTuyen> diemCongs, int batchSize);
    
    void deleteAll();
}
