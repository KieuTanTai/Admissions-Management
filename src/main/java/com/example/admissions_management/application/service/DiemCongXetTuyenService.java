package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.DiemCongImportRequest;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DiemCongXetTuyenService {

    private final DiemCongXetTuyenRepository diemCongRepository;

    public DiemCongXetTuyenService(DiemCongXetTuyenRepository diemCongRepository) {
        this.diemCongRepository = diemCongRepository;
    }

    /**
     * Lấy tất cả điểm cộng
     */
    public List<DiemCongXetTuyen> getAll() {
        return diemCongRepository.findAll();
    }

    /**
     * Tìm điểm cộng theo CCCD
     */
    public List<DiemCongXetTuyen> findByTsCccd(String tsCccd) {
        return diemCongRepository.findByTsCccd(tsCccd);
    }

    /**
     * Tìm điểm cộng theo mã ngành
     */
    public List<DiemCongXetTuyen> findByMaNganh(String maNganh) {
        return diemCongRepository.findByMaNganh(maNganh);
    }

    /**
     * Tìm điểm cộng theo ngành + tổ hợp + CCCD
     */
    public List<DiemCongXetTuyen> findByTsCccdAndMaNganhAndMaToHop(String tsCccd, String maNganh, String maToHop) {
        return diemCongRepository.findByTsCccdAndMaNganhAndMaToHop(tsCccd, maNganh, maToHop);
    }

    /**
     * Lấy điểm cộng theo khóa duy nhất
     */
    public Optional<DiemCongXetTuyen> findByDcKeys(String dcKeys) {
        return diemCongRepository.findByDcKeys(dcKeys);
    }

    /**
     * Thêm hoặc cập nhật điểm cộng đơn
     */
    @Transactional
    public DiemCongXetTuyen saveOrUpdate(DiemCongImportRequest request) {
        String dcKeys = generateDcKeys(request.getTsCccd(), request.getMaNganh(), request.getMaToHop());
        
        Optional<DiemCongXetTuyen> existing = diemCongRepository.findByDcKeys(dcKeys);
        
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

    /**
     * Import hàng loạt từ Excel
     */
    @Transactional
    public int importFromExcel(List<DiemCongImportRequest> records) {
        int count = 0;
        for (DiemCongImportRequest record : records) {
            try {
                saveOrUpdate(record);
                count++;
            } catch (Exception e) {
                // Log error, continue processing
                System.err.println("Error importing record: " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * Batch upsert: process records in batch, resolve existing by dcKeys and saveAll.
     */
    @Transactional
    public DiemCongImportSummary importInBatches(List<DiemCongImportRequest> records, int batchSize) {
        DiemCongImportSummary summary = new DiemCongImportSummary();
        summary.setTotalRows(records == null ? 0 : records.size());

        if (records == null || records.isEmpty()) {
            return summary;
        }

        for (int start = 0; start < records.size(); start += batchSize) {
            int end = Math.min(start + batchSize, records.size());
            List<DiemCongImportRequest> batch = records.subList(start, end);

            // build dcKeys
            List<String> keys = batch.stream()
                    .map(r -> generateDcKeys(r.getTsCccd(), r.getMaNganh(), r.getMaToHop()))
                    .toList();

            // fetch existing
            List<DiemCongXetTuyen> existing = diemCongRepository.findByDcKeysIn(keys);
            java.util.Map<String, DiemCongXetTuyen> existingMap = existing.stream()
                    .collect(java.util.stream.Collectors.toMap(DiemCongXetTuyen::getDcKeys, e -> e));

            List<DiemCongXetTuyen> toSave = new java.util.ArrayList<>();
            for (DiemCongImportRequest r : batch) {
                String key = generateDcKeys(r.getTsCccd(), r.getMaNganh(), r.getMaToHop());

                DiemCongXetTuyen d = null;
                boolean updated = false;

                if (r.getId() != null) {
                    java.util.Optional<DiemCongXetTuyen> byId = diemCongRepository.findById(r.getId());
                    if (byId.isPresent()) {
                        d = byId.get();
                        updated = true;
                    }
                }

                if (d == null) {
                    d = existingMap.get(key);
                    if (d != null) {
                        updated = true;
                    }
                }

                if (d == null) {
                    d = new DiemCongXetTuyen();
                    if (r.getId() != null) {
                        d.setId(r.getId());
                    }
                }

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

                if (updated) {
                    summary.setUpdatedCount(summary.getUpdatedCount() + 1);
                } else {
                    summary.setNewCount(summary.getNewCount() + 1);
                }
            }

            diemCongRepository.saveAll(toSave);
        }
        return summary;
    }

    /**
     * Xóa điểm cộng
     */
    @Transactional
    public void delete(Long id) {
        diemCongRepository.delete(id);
    }

    /**
     * Xóa tất cả dữ liệu điểm cộng
     */
    @Transactional
    public void deleteAll() {
        diemCongRepository.deleteAll();
    }

    /**
     * Tạo khóa duy nhất: CCCD_MANGANH_MATOHOP
     */
    private String generateDcKeys(String tsCccd, String maNganh, String maToHop) {
        return tsCccd + "_" + maNganh + "_" + (maToHop != null ? maToHop : "");
    }
}
