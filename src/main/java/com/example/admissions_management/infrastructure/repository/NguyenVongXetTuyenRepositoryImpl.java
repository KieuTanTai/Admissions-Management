package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.domain.repository.NguyenVongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNguyenVongXetTuyenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NguyenVongXetTuyenRepositoryImpl implements NguyenVongXetTuyenRepository {

    private final SpringDataXtNguyenVongXetTuyenRepository springDataRepository;

    public NguyenVongXetTuyenRepositoryImpl(SpringDataXtNguyenVongXetTuyenRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public List<NguyenVongXetTuyen> findAll() {
        return springDataRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
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

    private NguyenVongXetTuyen toDomain(XtNguyenVongXetTuyenEntity entity) {
        NguyenVongXetTuyen domain = new NguyenVongXetTuyen();
        domain.setId(entity.getId());
        domain.setNvMaNganh(entity.getNvMaNganh());
        domain.setNnCccd(entity.getNnCccd());
        domain.setMaToHop(extractMaToHop(entity.getNvKeys()));
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
        entity.setNnCccd(domain.getNnCccd());
        entity.setNvMaNganh(domain.getNvMaNganh());
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

    private String extractMaToHop(String nvKeys) {
        if (nvKeys == null || nvKeys.isBlank()) {
            return "";
        }

        String[] parts = nvKeys.split("_");
        if (parts.length < 4) {
            return "";
        }

        return parts[parts.length - 2].trim();
    }
}
