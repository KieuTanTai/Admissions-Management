package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.domain.repository.BangQuyDoiRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtBangQuyDoiEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtBangQuyDoiRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BangQuyDoiRepositoryImpl implements BangQuyDoiRepository {

    private final SpringDataXtBangQuyDoiRepository springData;

    public BangQuyDoiRepositoryImpl(SpringDataXtBangQuyDoiRepository springData) {
        this.springData = springData;
    }

    @Override
    public List<BangQuyDoi> findAll() {
        return springData.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<BangQuyDoi> findById(Integer id) {
        return springData.findById(id).map(this::toDomain);
    }

    @Override
    public List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi) {
        return springData.findByMaQuyDoi(maQuyDoi).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public BangQuyDoi save(BangQuyDoi bqd) {
        XtBangQuyDoiEntity e = toEntity(bqd);
        return toDomain(springData.save(e));
    }

    @Override
    public void delete(Integer id) {
        springData.deleteById(id);
    }

    @Override
    public void deleteAll() {
        springData.deleteAll();
    }

    @Override
    public void saveAll(List<BangQuyDoi> items) {
        List<XtBangQuyDoiEntity> entities = items.stream().map(this::toEntity).collect(Collectors.toList());
        springData.saveAll(entities);
    }

    private BangQuyDoi toDomain(XtBangQuyDoiEntity e) {
        return new BangQuyDoi(
                e.getId(),
                e.getPhuongThuc(),
                e.getToHop(),
                e.getMon(),
                e.getDiemA(),
                e.getDiemB(),
                e.getDiemC(),
                e.getDiemD(),
                e.getMaQuyDoi(),
                e.getPhanVi()
        );
    }

    private XtBangQuyDoiEntity toEntity(BangQuyDoi d) {
        XtBangQuyDoiEntity e = new XtBangQuyDoiEntity();
        e.setId(d.getId());
        e.setPhuongThuc(d.getPhuongThuc());
        e.setToHop(d.getToHop());
        e.setMon(d.getMon());
        e.setDiemA(d.getDiemA());
        e.setDiemB(d.getDiemB());
        e.setDiemC(d.getDiemC());
        e.setDiemD(d.getDiemD());
        e.setMaQuyDoi(d.getMaQuyDoi());
        e.setPhanVi(d.getPhanVi());
        return e;
    }
}
