package com.example.admissions_management.infrastructure.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.domain.repository.BangQuyDoiRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtBangQuyDoiEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataBangQuyDoiRepository;

@Repository
public class BangQuyDoiRepositoryImpl implements BangQuyDoiRepository {

    // Sử dụng sức mạnh của Spring Data JPA
    private final SpringDataBangQuyDoiRepository springDataRepository;

    public BangQuyDoiRepositoryImpl(SpringDataBangQuyDoiRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    // --- HÀM CHUYỂN ĐỔI: TỪ ENTITY (DB) SANG MODEL (DOMAIN) ---
    private BangQuyDoi toDomain(XtBangQuyDoiEntity entity) {
        if (entity == null) return null;
        return new BangQuyDoi(
            entity.getId(), entity.getPhuongThuc(), entity.getToHop(),
            entity.getMon(), entity.getDiemA(), entity.getDiemB(),
            entity.getDiemC(), entity.getDiemD(), entity.getMaQuyDoi(),
            entity.getPhanVi()
        );
    }

    // --- HÀM CHUYỂN ĐỔI: TỪ MODEL (DOMAIN) SANG ENTITY (DB) ---
    private XtBangQuyDoiEntity toEntity(BangQuyDoi domain) {
        if (domain == null) return null;
        XtBangQuyDoiEntity entity = new XtBangQuyDoiEntity();
        entity.setId(domain.getId());
        entity.setPhuongThuc(domain.getPhuongThuc());
        entity.setToHop(domain.getToHop());
        entity.setMon(domain.getMon());
        entity.setDiemA(domain.getDiemA());
        entity.setDiemB(domain.getDiemB());
        entity.setDiemC(domain.getDiemC());
        entity.setDiemD(domain.getDiemD());
        entity.setMaQuyDoi(domain.getMaQuyDoi());
        entity.setPhanVi(domain.getPhanVi());
        return entity;
    }

    @Override
    public Optional<BangQuyDoi> timQuyTacChinhXac(String phuongThuc, String mon, BigDecimal diemGoc) {
        // Dùng hàm bạn vừa định nghĩa bên SpringDataBangQuyDoiRepository
        return springDataRepository.timQuyTacChinhXac(phuongThuc, mon, diemGoc).map(this::toDomain);
    }

    @Override
    public Optional<BangQuyDoi> timQuyTacTheoKhoang(String phuongThuc, BigDecimal diemGoc) {
         // Dùng hàm bạn vừa định nghĩa bên SpringDataBangQuyDoiRepository
        return springDataRepository.timQuyTacTheoKhoang(phuongThuc, diemGoc).map(this::toDomain);
    }

    @Override
    @Transactional
    public BangQuyDoi add(BangQuyDoi quyTac) {
        XtBangQuyDoiEntity entity = toEntity(quyTac);
        XtBangQuyDoiEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    @Transactional
    public BangQuyDoi update(BangQuyDoi quyTac)
    {
        XtBangQuyDoiEntity entity = toEntity(quyTac);
        XtBangQuyDoiEntity updatedEntity = springDataRepository.saveAndFlush(entity);
        return toDomain(updatedEntity);
    }

    @Override
    @Transactional
    public List<BangQuyDoi> getAll()
    {
        return springDataRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<BangQuyDoi> findById(Integer id) {
        // Hàm findById() đã có sẵn trong JpaRepository
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi){
        return springDataRepository.timTheoMaQuyDoi(maQuyDoi).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        // Hàm deleteById() đã có sẵn trong JpaRepository
        springDataRepository.deleteById(id);
    }
}