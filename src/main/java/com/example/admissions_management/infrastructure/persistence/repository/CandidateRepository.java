package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<XtThiSinhXetTuyen25Entity, Integer> {

    Optional<XtThiSinhXetTuyen25Entity> findByCccd(String cccd);
    List<XtThiSinhXetTuyen25Entity> findByCccdIn(List<String> cccdList);
    Page<XtThiSinhXetTuyen25Entity> findByCccdContainingIgnoreCaseOrHoContainingIgnoreCaseOrTenContainingIgnoreCase(
            String cccd, String ho, String ten, Pageable pageable);

}
