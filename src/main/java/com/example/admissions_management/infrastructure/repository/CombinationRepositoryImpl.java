package com.example.admissions_management.infrastructure.repository;

import com.example.admissions_management.domain.model.Combination;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhToHopEntity;
import com.example.admissions_management.infrastructure.persistence.repository.ISpringDataCombinationRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
public class CombinationRepositoryImpl implements ICombinationRepository {

    private final ISpringDataCombinationRepository springDataCombinationRepository;

    public CombinationRepositoryImpl(ISpringDataCombinationRepository springDataCombinationRepository) {
        this.springDataCombinationRepository = springDataCombinationRepository;
    }


    @Override
    public List<Combination> findAll() {
        return springDataCombinationRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Combination> findByMajorCode(String majorCode) {
        if (majorCode == null || majorCode.isBlank())
            return findAll();
        return springDataCombinationRepository.findByMaNganhContaining(majorCode.trim()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Combination> findById(Integer id) {
        if (id == null)
            return Optional.empty();
        return springDataCombinationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Combination save(Combination combination) {
        XtNganhToHopEntity entity = toEntity(combination);
        XtNganhToHopEntity saved = springDataCombinationRepository.save(entity);
        return toDomain(saved);
    }

  @Override
  public Map<String, Object> findAllPaged(int page, int size) {
    if (page < 0) page = 0;
    if (size <= 0) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Page<XtNganhToHopEntity> result = springDataCombinationRepository.findAll(pageable);

      return getStringObjectMap(result);
  }

  @Override
  public Map<String, Object> findByMajorCodePaged(String majorCode, int page, int size) {
    if (page < 0) page = 0;
    if (size <= 0) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Page<XtNganhToHopEntity> result;

    if (majorCode == null || majorCode.isBlank()) {
      result = springDataCombinationRepository.findAll(pageable);
    } else {
      result = springDataCombinationRepository.findByMaNganhContaining(majorCode.trim(), pageable);
    }

      return getStringObjectMap(result);
  }

    @NonNull
    private Map<String, Object> getStringObjectMap(Page<XtNganhToHopEntity> result) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent().stream().map(this::toDomain).toList());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("hasNext", result.hasNext());
        response.put("hasPrevious", result.hasPrevious());

        return response;
    }

    private Combination toDomain(XtNganhToHopEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Combination(
                entity.getId(),
                entity.getMaNganh(),
                entity.getMaToHop(),
                entity.getMon1(),
                entity.getHeSoMon1(),
                entity.getMon2(),
                entity.getHeSoMon2(),
                entity.getMon3(),
                entity.getHeSoMon3(),
                entity.getTbKeys(),
                entity.getN1(),
                entity.getTo(),
                entity.getLi(),
                entity.getHo(),
                entity.getSi(),
                entity.getVa(),
                entity.getSu(),
                entity.getDi(),
                entity.getTi(),
                entity.getKhac(),
                entity.getKtpl(),
                entity.getDoLech()
        );
    }

    private XtNganhToHopEntity toEntity(Combination combination) {
        if (combination == null) {
            throw new IllegalArgumentException("combination is required");
        }
        XtNganhToHopEntity entity = new XtNganhToHopEntity();
        entity.setId(combination.getId());
        entity.setMaNganh(combination.getMaNganh());
        entity.setMaToHop(combination.getMaToHop());
        entity.setMon1(combination.getThMon1());
        entity.setHeSoMon1(combination.getHsMon1());
        entity.setMon2(combination.getThMon2());
        entity.setHeSoMon2(combination.getHsMon2());
        entity.setMon3(combination.getThMon3());
        entity.setHeSoMon3(combination.getHsMon3());
        entity.setTbKeys(combination.getTbKeys());
        entity.setN1(combination.getN1());
        entity.setTo(combination.getTo());
        entity.setLi(combination.getLi());
        entity.setHo(combination.getHo());
        entity.setSi(combination.getSi());
        entity.setVa(combination.getVa());
        entity.setSu(combination.getSu());
        entity.setDi(combination.getDi());
        entity.setTi(combination.getTi());
        entity.setKhac(combination.getKhac());
        entity.setKtpl(combination.getKtpl());
        entity.setDoLech(combination.getDoLech());
        return entity;
    }
}
