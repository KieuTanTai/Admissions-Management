package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.application.service.AdminCombinationServiceMock;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CombinationFormController {

    private final AdminCombinationServiceMock service;

    public CombinationFormController(AdminCombinationServiceMock service) {
        this.service = service;
    }

    public List<CombinationResponse> loadAll() {
        return service.getAll();
    }

    public List<CombinationResponse> find(String query) {
        return service.find(query);
    }

    public CombinationResponse insert(CombinationResponse request) {
        return service.insert(request);
    }

    public CombinationResponse update(Integer id, CombinationResponse request) {
        return service.update(id, request);
    }

    public void delete(Integer id) {
        service.delete(id);
    }
}

