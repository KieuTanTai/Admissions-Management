package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataXtDiemThiXetTuyenRepository extends JpaRepository<XtDiemThiXetTuyenEntity, Integer> {

    Optional<XtDiemThiXetTuyenEntity> findByCccd(String cccd);

    List<XtDiemThiXetTuyenEntity> findAllByOrderByIdDesc();
}
