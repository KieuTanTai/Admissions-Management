package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemCongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemCongXetTuyenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DiemCongXetTuyenRepositoryImpl implements DiemCongXetTuyenRepository {

    private final SpringDataXtDiemCongXetTuyenRepository springDataRepository;

    public DiemCongXetTuyenRepositoryImpl(SpringDataXtDiemCongXetTuyenRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
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
