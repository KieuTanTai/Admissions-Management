package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.DiemCongImportRequest;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiemCongXetTuyenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiemCongXetTuyenService.class);

    private final DiemCongXetTuyenRepository diemCongRepository;

    public DiemCongXetTuyenService(DiemCongXetTuyenRepository diemCongRepository) {
        this.diemCongRepository = diemCongRepository;
    }

    public List<DiemCongXetTuyen> getAll() {
        return diemCongRepository.findAll();
    }

    public List<DiemCongXetTuyen> findByTsCccd(String tsCccd) {
        return diemCongRepository.findByTsCccd(tsCccd);
    }

    public List<DiemCongXetTuyen> findByMaNganh(String maNganh) {
        return diemCongRepository.findByMaNganh(maNganh);
    }

    public List<DiemCongXetTuyen> findByTsCccdAndMaNganhAndMaToHop(String tsCccd, String maNganh, String maToHop) {
        return diemCongRepository.findByTsCccdAndMaNganhAndMaToHop(tsCccd, maNganh, maToHop);
    }

    public java.util.Optional<DiemCongXetTuyen> findByDcKeys(String dcKeys) {
        return diemCongRepository.findByDcKeys(dcKeys);
    }

    public java.util.Optional<DiemCongXetTuyen> findById(Long id) {
        return diemCongRepository.findById(id);
    }

    @Transactional
    public DiemCongXetTuyen saveOrUpdate(DiemCongImportRequest request) {
        String dcKeys = generateDcKeys(request.getTsCccd(), request.getMaNganh(), request.getMaToHop());

        java.util.Optional<DiemCongXetTuyen> existing = diemCongRepository.findByDcKeys(dcKeys);

        DiemCongXetTuyen diemCong = existing.orElseGet(DiemCongXetTuyen::new);
        if (diemCong.getId() == null && request.getId() != null) {
            diemCong.setId(request.getId());
        }
        diemCong.setTsCccd(request.getTsCccd());
        diemCong.setMaNganh(request.getMaNganh());
        diemCong.setMaToHop(request.getMaToHop());
        diemCong.setPhuongThuc(request.getPhuongThuc());
        diemCong.setDiemCc(request.getDiemCc());
        diemCong.setDiemUtxt(request.getDiemUtxt());
        diemCong.setDiemTong(request.getDiemTong());
        diemCong.setGhiChu(request.getGhiChu());
        diemCong.setDcKeys(dcKeys);

        return diemCongRepository.save(diemCong);
    }

    @Transactional
    public int importFromExcel(List<DiemCongImportRequest> records) {
        int count = 0;
        if (records == null) return 0;
        for (DiemCongImportRequest record : records) {
            try {
                saveOrUpdate(record);
                count++;
            } catch (Exception e) {
                LOGGER.error("Error importing record: {}", e.getMessage());
            }
        }
        return count;
    }

    public DiemCongImportSummary importInBatches(List<DiemCongImportRequest> records, int batchSize) {
        DiemCongImportSummary summary = new DiemCongImportSummary();
        summary.setTotalRows(records == null ? 0 : records.size());

        if (records == null || records.isEmpty()) {
            return summary;
        }

        int effectiveBatch = Math.max(1, batchSize);
        for (int start = 0; start < records.size(); start += effectiveBatch) {
            int end = Math.min(start + effectiveBatch, records.size());
            List<DiemCongImportRequest> batch = records.subList(start, end);

            List<DiemCongXetTuyen> toSave = new java.util.ArrayList<>();
            for (DiemCongImportRequest r : batch) {
                String key = generateDcKeys(r.getTsCccd(), r.getMaNganh(), r.getMaToHop());
                DiemCongXetTuyen d = new DiemCongXetTuyen();
                d.setId(r.getId());
                d.setTsCccd(r.getTsCccd());
                d.setMaNganh(r.getMaNganh());
                d.setMaToHop(r.getMaToHop());
                d.setPhuongThuc(r.getPhuongThuc());
                d.setDiemCc(r.getDiemCc());
                d.setDiemUtxt(r.getDiemUtxt());
                d.setDiemTong(r.getDiemTong());
                d.setGhiChu(r.getGhiChu());
                d.setDcKeys(key);
                toSave.add(d);
            }

            long t0 = System.currentTimeMillis();
            diemCongRepository.bulkUpsert(toSave, effectiveBatch);
            long took = System.currentTimeMillis() - t0;
            LOGGER.info("importInBatches: bulkUpsert batchSize={} persisted={} took={}ms", effectiveBatch, toSave.size(), took);

            summary.setNewCount(summary.getNewCount() + toSave.size());
        }

        return summary;
    }

    @Transactional
    public void delete(Long id) {
        diemCongRepository.delete(id);
    }

    @Transactional
    public void deleteAll() {
        diemCongRepository.deleteAll();
    }

    private String generateDcKeys(String tsCccd, String maNganh, String maToHop) {
        return tsCccd + "_" + maNganh + "_" + (maToHop != null ? maToHop : "");
    }
}
