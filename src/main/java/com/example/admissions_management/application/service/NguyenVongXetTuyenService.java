package com.example.admissions_management.application.service;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import com.example.admissions_management.domain.repository.NguyenVongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNganhRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NguyenVongXetTuyenService {

    private final NguyenVongXetTuyenRepository nguyenVongRepository;
    private final DiemCongXetTuyenRepository diemCongRepository;
    private final SpringDataXtNganhRepository nganhRepository;

    public NguyenVongXetTuyenService(NguyenVongXetTuyenRepository nguyenVongRepository,
            DiemCongXetTuyenRepository diemCongRepository,
            SpringDataXtNganhRepository nganhRepository) {
        this.nguyenVongRepository = nguyenVongRepository;
        this.diemCongRepository = diemCongRepository;
        this.nganhRepository = nganhRepository;
    }

    /**
     * Lấy tất cả nguyện vọng
     */
    public List<NguyenVongXetTuyen> getAll() {
        return nguyenVongRepository.findAll();
    }

    /**
     * Lấy nguyện vọng theo CCCD
     */
    public List<NguyenVongXetTuyen> getByNnCccd(String nnCccd) {
        return nguyenVongRepository.findByNnCccdOrderByNvThuTuAsc(nnCccd);
    }

    /**
     * Lấy kết quả xét tuyển theo mã ngành
     */
    public List<NguyenVongXetTuyen> getResultByMaNganh(String maNganh) {
        return nguyenVongRepository.findByNvMaNganhOrderByDiemXetTuyenDesc(maNganh);
    }

    /**
     * Tạo nguyện vọng (chưa tính điểm)
     */
    @Transactional
    public NguyenVongXetTuyen createNguyenVong(String nnCccd, String maNganh, String maToHop, Integer nvThuTu) {
        NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
        nv.setNnCccd(nnCccd);
        nv.setNvMaNganh(maNganh);
        nv.setMaToHop(maToHop);
        nv.setNvThuTu(nvThuTu);
        nv.setDiemThxt(BigDecimal.ZERO);
        nv.setDiemUtqd(BigDecimal.ZERO);
        nv.setDiemCong(BigDecimal.ZERO);
        nv.setDiemXetTuyen(BigDecimal.ZERO);
        nv.setNvKetQua("Đang xét");
        nv.setTtPhuongThuc("");
        nv.setTtThm("");
        nv.setNvKeys(generateNvKeys(nnCccd, maNganh, maToHop, nvThuTu));
        nv.setCreatedAt(LocalDateTime.now());
        nv.setUpdatedAt(LocalDateTime.now());

        return nguyenVongRepository.save(nv);
    }

    /**
     * Tính điểm cho nguyện vọng từ các bảng khác
     * Công thức: diemXetTuyen = diemThxt + diemUtqd + diemCong
     */
    @Transactional
    public NguyenVongXetTuyen calculateScore(Integer nvId, BigDecimal diemThxt, BigDecimal diemUtqd) {
        Optional<NguyenVongXetTuyen> opt = nguyenVongRepository.findAll().stream()
                .filter(nv -> nv.getId().equals(nvId))
                .findFirst();

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Nguyện vọng không tìm thấy: " + nvId);
        }

        NguyenVongXetTuyen nv = opt.get();

        // Lấy điểm cộng
        BigDecimal diemCong = getDiemCongForNguyenVong(nv.getNnCccd(), nv.getNvMaNganh(), nv.getMaToHop());

        // Tính tổng
        BigDecimal totalScore = (diemThxt != null ? diemThxt : BigDecimal.ZERO)
                .add(diemUtqd != null ? diemUtqd : BigDecimal.ZERO)
                .add(diemCong);

        nv.setDiemThxt(diemThxt != null ? diemThxt : BigDecimal.ZERO);
        nv.setDiemUtqd(diemUtqd != null ? diemUtqd : BigDecimal.ZERO);
        nv.setDiemCong(diemCong);
        nv.setDiemXetTuyen(totalScore);
        nv.setUpdatedAt(LocalDateTime.now());

        return nguyenVongRepository.update(nv);
    }

    /**
     * Chạy quá trình xét tuyển cho một ngành
     * Logic:
     * 1. Lấy danh sách nguyện vọng cho ngành này
     * 2. Sắp xếp giảm dần theo điểm
     * 3. Lấy chỉ tiêu
     * 4. Cập nhật kết quả (Đậu/Trượt)
     */
    @Transactional
    public Map<String, Object> performSelectionForMajor(String maNganh) {
        // Lấy thông tin ngành
        Optional<XtNganhEntity> nganhOpt = nganhRepository.findByMaNganh(maNganh);
        if (nganhOpt.isEmpty()) {
            throw new IllegalArgumentException("Ngành không tìm thấy: " + maNganh);
        }

        XtNganhEntity nganh = nganhOpt.get();
        Integer chiTieu = nganh.getChiTieu();
        BigDecimal diemSan = nganh.getDiemSan();

        // Lấy danh sách nguyện vọng cho ngành này, sắp xếp theo điểm giảm dần
        List<NguyenVongXetTuyen> nguyenVongs = nguyenVongRepository.findByNvMaNganhOrderByDiemXetTuyenDesc(maNganh);

        // Cập nhật kết quả
        int passCount = 0;
        for (NguyenVongXetTuyen nv : nguyenVongs) {
            if (nv.getDiemXetTuyen().compareTo(diemSan) < 0) {
                // Điểm < sàn → Trượt
                nv.setNvKetQua("Trượt");
            } else if (passCount < chiTieu) {
                // Có chỉ tiêu → Đậu
                nv.setNvKetQua("Đậu");
                passCount++;
            } else {
                // Hết chỉ tiêu → Trượt
                nv.setNvKetQua("Trượt");
            }
            nv.setUpdatedAt(LocalDateTime.now());
            nguyenVongRepository.update(nv);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("maNganh", maNganh);
        result.put("chiTieu", chiTieu);
        result.put("diemSan", diemSan);
        result.put("totalApplicants", nguyenVongs.size());
        result.put("passCount", passCount);
        result.put("failCount", nguyenVongs.size() - passCount);

        return result;
    }

    /**
     * Chạy xét tuyển cho tất cả ngành
     */
    @Transactional
    public List<Map<String, Object>> performSelectionForAllMajors() {
        List<XtNganhEntity> allMajors = nganhRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();

        for (XtNganhEntity major : allMajors) {
            try {
                Map<String, Object> result = performSelectionForMajor(major.getMaNganh());
                results.add(result);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("maNganh", major.getMaNganh());
                error.put("error", e.getMessage());
                results.add(error);
            }
        }

        return results;
    }

    /**
     * Lấy danh sách đậu theo mã ngành
     */
    public List<NguyenVongXetTuyen> getPassedApplicants(String maNganh) {
        return nguyenVongRepository.findByNvMaNganh(maNganh).stream()
                .filter(nv -> "Đậu".equals(nv.getNvKetQua()))
                .sorted(Comparator.comparing(NguyenVongXetTuyen::getDiemXetTuyen).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách trượt theo mã ngành
     */
    public List<NguyenVongXetTuyen> getFailedApplicants(String maNganh) {
        return nguyenVongRepository.findByNvMaNganh(maNganh).stream()
                .filter(nv -> "Trượt".equals(nv.getNvKetQua()))
                .collect(Collectors.toList());
    }

    /**
     * Xóa nguyện vọng
     */
    @Transactional
    public void deleteNguyenVong(Integer id) {
        nguyenVongRepository.delete(id);
    }

    /**
     * Xóa tất cả nguyện vọng
     */
    @Transactional
    public void deleteAll() {
        nguyenVongRepository.deleteAll();
    }

    /**
     * Lấy điểm cộng cho nguyện vọng
     */
    private BigDecimal getDiemCongForNguyenVong(String tsCccd, String maNganh, String maToHop) {
        List<DiemCongXetTuyen> diemCongs = diemCongRepository.findByTsCccdAndMaNganhAndMaToHop(tsCccd, maNganh,
                maToHop);

        if (diemCongs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Lấy tổng điểm nếu có nhiều record
        return diemCongs.stream()
                .map(dc -> dc.getDiemTong() != null ? dc.getDiemTong() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tạo khóa duy nhất: CCCD_MANGANH_MATOHOP_TT
     */
    private String generateNvKeys(String nnCccd, String maNganh, String maToHop, Integer nvThuTu) {
        return nnCccd + "_" + maNganh + "_" + (maToHop != null ? maToHop : "") + "_" + nvThuTu;
    }
}
