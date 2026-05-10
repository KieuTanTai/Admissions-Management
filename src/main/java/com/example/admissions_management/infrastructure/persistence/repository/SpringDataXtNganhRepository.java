package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataXtNganhRepository extends JpaRepository<XtNganhEntity, Integer> {
    
    Optional<XtNganhEntity> findByMaNganh(String maNganh);
}
