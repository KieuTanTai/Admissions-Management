package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataXtNguyenVongXetTuyenRepository extends JpaRepository<XtNguyenVongXetTuyenEntity, Integer> {
    
    List<XtNguyenVongXetTuyenEntity> findByNnCccd(String nnCccd);
    
    List<XtNguyenVongXetTuyenEntity> findByMaNganh(String maNganh);
    
    Optional<XtNguyenVongXetTuyenEntity> findByNvKeys(String nvKeys);
    
    List<XtNguyenVongXetTuyenEntity> findByNvKetQua(String nvKetQua);
    
    @Query("SELECT nv FROM XtNguyenVongXetTuyenEntity nv WHERE nv.maNganh = ?1 ORDER BY nv.diemXetTuyen DESC")
    List<XtNguyenVongXetTuyenEntity> findByMaNganhOrderByDiemXetTuyenDesc(String maNganh);
    
    @Query("SELECT nv FROM XtNguyenVongXetTuyenEntity nv WHERE nv.nnCccd = ?1 ORDER BY nv.nvThuTu ASC")
    List<XtNguyenVongXetTuyenEntity> findByNnCccdOrderByNvThuTuAsc(String nnCccd);
}
