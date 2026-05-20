package com.example.admissions_management.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.admissions_management.infrastructure.persistence.entity.PreferenceEntity;

@Repository
public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {

    @Query("SELECT p.maNganh AS maNganh, p.phuongThuc AS phuongThuc, SUM(p.soLuong) AS sl " +
           "FROM PreferenceEntity p " +
           "WHERE p.maNganh IN :maNganhList " +
           "GROUP BY p.maNganh, p.phuongThuc")
    List<PreferenceCount> countByMajorAndMethod(@Param("maNganhList") List<String> maNganhList);

    interface PreferenceCount {
        String getMaNganh();
        String getPhuongThuc();
        Integer getSl();
    }
}