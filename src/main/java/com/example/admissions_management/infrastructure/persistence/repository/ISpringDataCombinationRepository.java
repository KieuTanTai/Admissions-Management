package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhToHopEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISpringDataCombinationRepository extends JpaRepository<XtNganhToHopEntity, Integer> {
	Page<XtNganhToHopEntity> findByMaNganhContaining(String majorCode, Pageable pageable);
	List<XtNganhToHopEntity> findByMaNganhContaining(String majorCode);
}
