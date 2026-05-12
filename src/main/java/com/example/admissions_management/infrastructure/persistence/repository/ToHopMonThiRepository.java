package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtToHopMonThiEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ToHopMonThiRepository extends JpaRepository<XtToHopMonThiEntity, Integer> {

    Page<XtToHopMonThiEntity> findByMaToHopContainingIgnoreCaseOrTenToHopContainingIgnoreCase(
            String maToHop,
            String tenToHop,
            Pageable pageable
    );

    Optional<XtToHopMonThiEntity> findByMaToHop(String maToHop);

    List<XtToHopMonThiEntity> findByMaToHopIn(Collection<String> maToHopList);
}