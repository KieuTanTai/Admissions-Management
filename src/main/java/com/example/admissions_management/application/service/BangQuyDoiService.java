package com.example.admissions_management.application.service;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.domain.repository.BangQuyDoiRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BangQuyDoiService {

    private final BangQuyDoiRepository repository;

    public BangQuyDoiService(BangQuyDoiRepository repository) {
        this.repository = repository;
    }

    public List<BangQuyDoi> getAll() {
        return repository.findAll();
    }

    public List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi) {
        return repository.findByMaQuyDoi(maQuyDoi);
    }

    public BangQuyDoi saveOrUpdate(BangQuyDoi bqd) {
        return repository.save(bqd);
    }

    public void delete(Integer id) {
        repository.delete(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void saveAll(List<BangQuyDoi> items) {
        repository.saveAll(items);
    }
}
