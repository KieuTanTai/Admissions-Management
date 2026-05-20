package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.Applicant;
import com.example.admissions_management.domain.repository.ApplicantRepository;
import com.example.admissions_management.infrastructure.persistence.entity.ApplicantEntity;
import com.example.admissions_management.infrastructure.persistence.repository.ISpringDataApplicantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicantRepositoryImpl implements ApplicantRepository {

    private final ISpringDataApplicantRepository springDataApplicantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ApplicantRepositoryImpl(ISpringDataApplicantRepository springDataApplicantRepository) {
        this.springDataApplicantRepository = springDataApplicantRepository;
    }

    @Override
    public List<Applicant> findAll() {
        return springDataApplicantRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Applicant> findByEmail(String email) {
        return springDataApplicantRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Applicant save(Applicant applicant) {
        ApplicantEntity entity = new ApplicantEntity();
        entity.setId(applicant.getId());
        entity.setFullName(applicant.getFullName());
        entity.setEmail(applicant.getEmail());
        entity.setProgram(applicant.getProgram());

        ApplicantEntity saved = springDataApplicantRepository.save(entity);
        return toDomain(saved);
    }

    private Applicant toDomain(ApplicantEntity entity) {
        return new Applicant(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getProgram()
        );
    }

    // --- Bổ sung phương thức lấy điểm trúng tuyển thấp nhất theo ngành ---
    @Override
    public List<MinScorePerMajor> findMinScorePerMajor(List<String> maNganhList) {
        String jpql = "SELECT a.maNganh, MIN(a.diemTongKet) " +
                      "FROM ApplicantEntity a " +
                      "WHERE a.maNganh IN :maNganhList " +
                      "GROUP BY a.maNganh";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("maNganhList", maNganhList);

        List<Object[]> results = query.getResultList();
        List<MinScorePerMajor> list = new ArrayList<>();
        for (Object[] r : results) {
            list.add(new MinScorePerMajor() {
                @Override
                public String getMaNganh() {
                    return (String) r[0];
                }

                @Override
                public BigDecimal getMinDiem() {
                    return (BigDecimal) r[1];
                }
            });
        }
        return list;
    }
}