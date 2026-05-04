package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.Combination;
import java.util.Map;

import java.util.List;
import java.util.Optional;

public interface ICombinationRepository {
    List<Combination> findAll();
    Combination save(Combination combination);

    List<Combination> findByMajorCode(String majorCode);

    Optional<Combination> findById(Integer id);
    Map<String, Object> findAllPaged(int page, int size);
    Map<String, Object> findByMajorCodePaged(String majorCode, int page, int size);
}
