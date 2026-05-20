package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.Applicant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicantRepository {

    List<Applicant> findAll();

    Optional<Applicant> findByEmail(String email);

    Applicant save(Applicant applicant);
    @Query("SELECT a.maNganh AS maNganh, MIN(a.diemTongKet) AS minDiem " +
       "FROM ApplicantEntity a " +
       "WHERE a.maNganh IN :maNganhList " +
       "GROUP BY a.maNganh")
    List<MinScorePerMajor> findMinScorePerMajor(@Param("maNganhList") List<String> maNganhList);

    interface MinScorePerMajor {
        String getMaNganh();
        BigDecimal getMinDiem();
    }
}
