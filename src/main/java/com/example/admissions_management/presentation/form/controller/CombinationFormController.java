package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.application.service.AdminCombinationService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

@Component
public class CombinationFormController {

  private final AdminCombinationService service;

  public CombinationFormController(AdminCombinationService service) {
        this.service = service;
    }

    public CombinationResponse insert(CombinationResponse request) {
        return service.insert(request);
    }

    public CombinationResponse update(Integer id, CombinationResponse request) {
        return service.update(id, request);
    }

      public Map<String, Object> loadAllPaged(int page) {
        return service.getAllPaged(page);
      }

      public Map<String, Object> findPaged(String query, int page) {
        return service.findByMajorCodePaged(query, page);
      }
}

