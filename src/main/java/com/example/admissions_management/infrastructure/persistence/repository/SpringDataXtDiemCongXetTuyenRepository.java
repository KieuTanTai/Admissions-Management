package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemCongXetTuyenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataXtDiemCongXetTuyenRepository extends JpaRepository<XtDiemCongXetTuyenEntity, Long> {
    
    List<XtDiemCongXetTuyenEntity> findByTsCccd(String tsCccd);
    
    List<XtDiemCongXetTuyenEntity> findByMaNganh(String maNganh);
    
    Optional<XtDiemCongXetTuyenEntity> findByDcKeys(String dcKeys);
    List<XtDiemCongXetTuyenEntity> findByDcKeysIn(List<String> dcKeys);
    
    List<XtDiemCongXetTuyenEntity> findByTsCccdAndMaNganhAndMaToHop(String tsCccd, String maNganh, String maToHop);
}
