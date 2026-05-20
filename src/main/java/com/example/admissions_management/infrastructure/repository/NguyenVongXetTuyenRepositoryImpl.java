package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.domain.repository.NguyenVongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNguyenVongXetTuyenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NguyenVongXetTuyenRepositoryImpl implements NguyenVongXetTuyenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NguyenVongXetTuyenRepositoryImpl.class);

    private final SpringDataXtNguyenVongXetTuyenRepository springDataRepository;
    private final DataSource dataSource;

    public NguyenVongXetTuyenRepositoryImpl(SpringDataXtNguyenVongXetTuyenRepository springDataRepository, DataSource dataSource) {
        this.springDataRepository = springDataRepository;
        this.dataSource = dataSource;
    }

    @Override
    public List<NguyenVongXetTuyen> findAll() {
        return springDataRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NguyenVongXetTuyen> findById(Integer id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<NguyenVongXetTuyen> findByNnCccd(String nnCccd) {
        return springDataRepository.findByNnCccd(nnCccd).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<NguyenVongXetTuyen> findByNvMaNganh(String nvMaNganh) {
        return springDataRepository.findByNvMaNganh(nvMaNganh).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NguyenVongXetTuyen> findByNvKeys(String nvKeys) {
        return springDataRepository.findByNvKeys(nvKeys).map(this::toDomain);
    }

    @Override
    public List<NguyenVongXetTuyen> findByNvKetQua(String nvKetQua) {
        return springDataRepository.findByNvKetQua(nvKetQua).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<NguyenVongXetTuyen> findByNvMaNganhOrderByDiemXetTuyenDesc(String nvMaNganh) {
        return springDataRepository.findByNvMaNganhOrderByDiemXetTuyenDesc(nvMaNganh).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<NguyenVongXetTuyen> findByNnCccdOrderByNvThuTuAsc(String nnCccd) {
        return springDataRepository.findByNnCccdOrderByNvThuTuAsc(nnCccd).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public NguyenVongXetTuyen save(NguyenVongXetTuyen nguyenVong) {
        XtNguyenVongXetTuyenEntity entity = toEntity(nguyenVong);
        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public NguyenVongXetTuyen update(NguyenVongXetTuyen nguyenVong) {
        XtNguyenVongXetTuyenEntity entity = toEntity(nguyenVong);
        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public void delete(Integer id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }

    @Override
    public void updateBatch(List<NguyenVongXetTuyen> nguyenVongs) {
        List<XtNguyenVongXetTuyenEntity> entities = nguyenVongs.stream()
                .map(this::toEntity)
                .toList();
        springDataRepository.saveAll(entities);
    }

    @Override
    public void bulkInsert(List<NguyenVongXetTuyen> nguyenVongs, int batchSize) {
        if (nguyenVongs == null || nguyenVongs.isEmpty()) return;
        String sql = "INSERT INTO xt_nguyenvongxettuyen (nn_cccd, nv_manganh, nv_tt, diem_thxt, diem_utqd, diem_cong, diem_xettuyen, nv_ketqua, nv_keys, tt_phuongthuc, tt_thm) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            int count = 0;
            int skipped = 0;
            for (NguyenVongXetTuyen nv : nguyenVongs) {
                XtNguyenVongXetTuyenEntity e = toEntity(nv);

                // Validate required NOT NULL columns in DB schema: nn_cccd, nv_manganh, nv_tt
                if (e.getNnCccd() == null || e.getNnCccd().trim().isEmpty()
                        || e.getNvMaNganh() == null || e.getNvMaNganh().trim().isEmpty()
                        || e.getNvThuTu() == null) {
                    skipped++;
                    continue;
                }

                ps.setString(1, e.getNnCccd());
                ps.setString(2, e.getNvMaNganh());
                ps.setInt(3, e.getNvThuTu());
                ps.setBigDecimal(4, e.getDiemThxt());
                ps.setBigDecimal(5, e.getDiemUtqd());
                ps.setBigDecimal(6, e.getDiemCong());
                ps.setBigDecimal(7, e.getDiemXetTuyen());
                ps.setString(8, e.getNvKetQua());
                ps.setString(9, e.getNvKeys());
                ps.setString(10, e.getTtPhuongThuc());
                ps.setString(11, e.getTtThm());
                ps.addBatch();
                count++;
                if (count % batchSize == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }
            if (count % batchSize != 0) {
                ps.executeBatch();
                conn.commit();
            }
            conn.setAutoCommit(true);
            if (skipped > 0) {
                LOGGER.info("bulkInsert: inserted={}, skipped={} (skipped rows had missing required fields)", count, skipped);
            } else {
                LOGGER.info("bulkInsert: inserted={}", count);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Bulk insert failed: " + ex.getMessage(), ex);
        }
    }

    private NguyenVongXetTuyen toDomain(XtNguyenVongXetTuyenEntity entity) {
        NguyenVongXetTuyen domain = new NguyenVongXetTuyen();
        domain.setId(entity.getId());
        domain.setNvMaNganh(entity.getNvMaNganh());
        domain.setNnCccd(entity.getNnCccd());
        domain.setNvThuTu(entity.getNvThuTu());
        domain.setDiemThxt(entity.getDiemThxt());
        domain.setDiemUtqd(entity.getDiemUtqd());
        domain.setDiemCong(entity.getDiemCong());
        domain.setDiemXetTuyen(entity.getDiemXetTuyen());
        domain.setNvKetQua(entity.getNvKetQua());
        domain.setTtPhuongThuc(entity.getTtPhuongThuc());
        domain.setTtThm(entity.getTtThm());
        domain.setNvKeys(entity.getNvKeys());
        return domain;
    }

    private XtNguyenVongXetTuyenEntity toEntity(NguyenVongXetTuyen domain) {
        XtNguyenVongXetTuyenEntity entity = new XtNguyenVongXetTuyenEntity();
        entity.setId(domain.getId());
        entity.setNvMaNganh(domain.getNvMaNganh());
        entity.setNnCccd(domain.getNnCccd());
        entity.setNvThuTu(domain.getNvThuTu());
        entity.setDiemThxt(domain.getDiemThxt());
        entity.setDiemUtqd(domain.getDiemUtqd());
        entity.setDiemCong(domain.getDiemCong());
        entity.setDiemXetTuyen(domain.getDiemXetTuyen());
        entity.setNvKetQua(domain.getNvKetQua());
        entity.setTtPhuongThuc(domain.getTtPhuongThuc());
        entity.setTtThm(domain.getTtThm());
        entity.setNvKeys(domain.getNvKeys());
        return entity;
    }
}
