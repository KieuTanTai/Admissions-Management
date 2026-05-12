package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MajorRepository extends JpaRepository<XtNganhEntity, Integer> {

    Page<XtNganhEntity> findByMaNganhContainingIgnoreCaseOrTenNganhContainingIgnoreCase(
            String maNganh,
            String tenNganh,
            Pageable pageable
    );

    Optional<XtNganhEntity> findByMaNganh(String maNganh);

    List<XtNganhEntity> findByMaNganhIn(Collection<String> maNganhList);
}