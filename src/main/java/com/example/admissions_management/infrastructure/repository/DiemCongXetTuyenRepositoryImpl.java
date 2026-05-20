package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemCongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemCongXetTuyenRepository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DiemCongXetTuyenRepositoryImpl implements DiemCongXetTuyenRepository {

    private final SpringDataXtDiemCongXetTuyenRepository springDataRepository;
    private final DataSource dataSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiemCongXetTuyenRepositoryImpl.class);

    public DiemCongXetTuyenRepositoryImpl(SpringDataXtDiemCongXetTuyenRepository springDataRepository, DataSource dataSource) {
        this.springDataRepository = springDataRepository;
        this.dataSource = dataSource;
    }

    @Override
    public List<DiemCongXetTuyen> findAll() {
        return springDataRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DiemCongXetTuyen> findByTsCccd(String tsCccd) {
        return springDataRepository.findByTsCccd(tsCccd).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DiemCongXetTuyen> findByMaNganh(String maNganh) {
        return springDataRepository.findByMaNganh(maNganh).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<DiemCongXetTuyen> findByDcKeys(String dcKeys) {
        return springDataRepository.findByDcKeys(dcKeys).map(this::toDomain);
    }

    @Override
    public Optional<DiemCongXetTuyen> findById(Long id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<DiemCongXetTuyen> findByTsCccdAndMaNganhAndMaToHop(String tsCccd, String maNganh, String maToHop) {
        return springDataRepository.findByTsCccdAndMaNganhAndMaToHop(tsCccd, maNganh, maToHop).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public DiemCongXetTuyen save(DiemCongXetTuyen diemCong) {
        XtDiemCongXetTuyenEntity entity = toEntity(diemCong);
        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public DiemCongXetTuyen update(DiemCongXetTuyen diemCong) {
        XtDiemCongXetTuyenEntity entity = toEntity(diemCong);
        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<DiemCongXetTuyen> findByDcKeysIn(List<String> dcKeys) {
        return springDataRepository.findByDcKeysIn(dcKeys).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<DiemCongXetTuyen> diemCongs) {
        List<XtDiemCongXetTuyenEntity> entities = diemCongs.stream()
                .map(this::toEntity)
                .toList();
        springDataRepository.saveAll(entities);
    }

    @Override
    public void bulkUpsert(List<DiemCongXetTuyen> diemCongs, int batchSize) {
        if (diemCongs == null || diemCongs.isEmpty()) return;

        try (Connection conn = dataSource.getConnection()) {
            final String quote = "\"";
            final String table = "xt_diemcongxetuyen";
            final String[] columns = new String[]{
                    "iddiemcong",
                    "ts_cccd",
                    "manganh",
                    "matohop",
                    "phuongthuc",
                    "diemcc",
                    "diemutxt",
                    "diemtong",
                    "ghichu",
                    "dc_keys"
            };

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO ").append(quote).append(table).append(quote).append(" (");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sqlBuilder.append(", ");
                sqlBuilder.append(quote).append(columns[i]).append(quote);
            }
            sqlBuilder.append(") VALUES (");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sqlBuilder.append(", ");
                sqlBuilder.append("?");
            }
            sqlBuilder.append(")");

            try (PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
                conn.setAutoCommit(false);
                int count = 0;
                int skipped = 0;

                for (DiemCongXetTuyen d : diemCongs) {
                    XtDiemCongXetTuyenEntity e = toEntity(d);

                    if (e.getTsCccd() == null || e.getTsCccd().trim().isEmpty()
                            || e.getMaNganh() == null || e.getMaNganh().trim().isEmpty()) {
                        skipped++;
                        continue;
                    }

                    if (e.getId() == null) {
                        ps.setNull(1, java.sql.Types.BIGINT);
                    } else {
                        ps.setLong(1, e.getId());
                    }
                    ps.setString(2, e.getTsCccd());
                    ps.setString(3, e.getMaNganh());
                    ps.setString(4, e.getMaToHop());
                    ps.setString(5, e.getPhuongThuc());
                    ps.setBigDecimal(6, e.getDiemCc());
                    ps.setBigDecimal(7, e.getDiemUtxt());
                    ps.setBigDecimal(8, e.getDiemTong());
                    ps.setString(9, e.getGhiChu());
                    ps.setString(10, e.getDcKeys());

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
                    LOGGER.info("bulkUpsert (insert-only): inserted={}, skipped={} (skipped rows had missing required fields)", count, skipped);
                } else {
                    LOGGER.info("bulkUpsert (insert-only): inserted={}", count);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Bulk insert failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }

    private DiemCongXetTuyen toDomain(XtDiemCongXetTuyenEntity entity) {
        return new DiemCongXetTuyen(
                entity.getId(),
                entity.getTsCccd(),
                entity.getMaNganh(),
                entity.getMaToHop(),
                entity.getPhuongThuc(),
                entity.getDiemCc(),
                entity.getDiemUtxt(),
                entity.getDiemTong(),
                entity.getGhiChu(),
                entity.getDcKeys()
        );
    }

    private XtDiemCongXetTuyenEntity toEntity(DiemCongXetTuyen domain) {
        XtDiemCongXetTuyenEntity entity = new XtDiemCongXetTuyenEntity();
        entity.setId(domain.getId());
        entity.setTsCccd(domain.getTsCccd());
        entity.setMaNganh(domain.getMaNganh());
        entity.setMaToHop(domain.getMaToHop());
        entity.setPhuongThuc(domain.getPhuongThuc());
        entity.setDiemCc(domain.getDiemCc());
        entity.setDiemUtxt(domain.getDiemUtxt());
        entity.setDiemTong(domain.getDiemTong());
        entity.setGhiChu(domain.getGhiChu());
        entity.setDcKeys(domain.getDcKeys());
        return entity;
    }
}
