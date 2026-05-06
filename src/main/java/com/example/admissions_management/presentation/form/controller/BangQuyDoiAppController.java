package com.example.admissions_management.presentation.form.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.admissions_management.application.service.BangQuyDoiService;
import com.example.admissions_management.domain.model.BangQuyDoi;

@Service
public class BangQuyDoiAppController {

    private final BangQuyDoiService bangQuyDoiService;

    public BangQuyDoiAppController(BangQuyDoiService bangQuyDoiService) {
        this.bangQuyDoiService = bangQuyDoiService;
    }

    public List<BangQuyDoi> getAll() {
        return bangQuyDoiService.getAll();
    }

    public BangQuyDoi add(BangQuyDoi quyTac)
    {
        return bangQuyDoiService.add(quyTac);
    }

    public void addList(List<BangQuyDoi> quyTacList)
    {
        for(BangQuyDoi qd: quyTacList)
        {
            bangQuyDoiService.add(qd);
        }
    }

    public Optional<BangQuyDoi> findById(int id) {
        return bangQuyDoiService.findById(id);
    }

    public List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi)
    {
        return bangQuyDoiService.findByMaQuyDoi(maQuyDoi);
    }
    
    public void delete(int id)
    {
        bangQuyDoiService.delete(id);
    }
}
