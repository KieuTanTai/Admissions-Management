package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtBangQuyDoiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataXtBangQuyDoiRepository extends JpaRepository<XtBangQuyDoiEntity, Integer> {
    List<XtBangQuyDoiEntity> findByMaQuyDoi(String maQuyDoi);
}
