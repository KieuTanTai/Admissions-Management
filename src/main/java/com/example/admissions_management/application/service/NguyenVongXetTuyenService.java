package com.example.admissions_management.application.service;

import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import com.example.admissions_management.domain.model.Combination;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import com.example.admissions_management.domain.repository.DiemCongXetTuyenRepository;
import com.example.admissions_management.domain.repository.NguyenVongXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtDiemThiXetTuyenRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNganhRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NguyenVongXetTuyenService {

    private final NguyenVongXetTuyenRepository nguyenVongRepository;
    private final DiemCongXetTuyenRepository diemCongRepository;
    private final SpringDataXtNganhRepository nganhRepository;
    private final SpringDataXtDiemThiXetTuyenRepository diemThiRepository;
    private final ICombinationRepository combinationRepository;
    private final BangQuyDoiService bangQuyDoiService;

    public NguyenVongXetTuyenService(NguyenVongXetTuyenRepository nguyenVongRepository,
            DiemCongXetTuyenRepository diemCongRepository,
            SpringDataXtNganhRepository nganhRepository,
            SpringDataXtDiemThiXetTuyenRepository diemThiRepository,
            ICombinationRepository combinationRepository,
            BangQuyDoiService bangQuyDoiService) {
        this.nguyenVongRepository = nguyenVongRepository;
        this.diemCongRepository = diemCongRepository;
        this.nganhRepository = nganhRepository;
        this.diemThiRepository = diemThiRepository;
        this.combinationRepository = combinationRepository;
        this.bangQuyDoiService = bangQuyDoiService;
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
     * SỬA LỖI: Tìm kiếm trực tiếp qua findById của Repository để tránh quá tải RAM khi data lớn
     */
    @Transactional
    public NguyenVongXetTuyen calculateScore(Integer nvId, BigDecimal diemThxt, BigDecimal diemUtqd) {
        Optional<NguyenVongXetTuyen> opt = nguyenVongRepository.findById(nvId);

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
     * SỬA LỖI: Tối ưu hóa truy vấn bằng findById thay vì kéo hết lên Stream lọc
     */
    @Transactional
    public NguyenVongXetTuyen updateNguyenVong(Integer id,
                                               String nnCccd,
                                               String maNganh,
                                               String maToHop,
                                               Integer nvThuTu,
                                               BigDecimal diemThxt,
                                               BigDecimal diemUtqd) {
        Optional<NguyenVongXetTuyen> opt = nguyenVongRepository.findById(id);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Nguyện vọng không tìm thấy: " + id);
        }

        NguyenVongXetTuyen nv = opt.get();
        nv.setNnCccd(nnCccd);
        nv.setNvMaNganh(maNganh);
        nv.setMaToHop(maToHop);
        nv.setNvThuTu(nvThuTu);
        nv.setNvKeys(generateNvKeys(nnCccd, maNganh, maToHop, nvThuTu));

        BigDecimal diemCong = getDiemCongForNguyenVong(nnCccd, maNganh, maToHop);
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
     * Import a batch of nguyện vọng using repository bulk insert (JDBC batch) for high throughput.
     */
    public com.example.admissions_management.application.dto.response.NguyenVongImportSummary importNguyenVongBatch(List<NguyenVongXetTuyen> batch, int batchSize) {
        com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary = new com.example.admissions_management.application.dto.response.NguyenVongImportSummary();
        if (batch == null || batch.isEmpty()) return summary;

        int total = 0, added = 0, updated = 0, skipped = 0;

        for (NguyenVongXetTuyen nv : batch) {
            total++;
            if (nv.getNnCccd() == null || nv.getNnCccd().trim().isEmpty()
                    || nv.getNvMaNganh() == null || nv.getNvMaNganh().trim().isEmpty()) {
                skipped++;
                continue;
            }

            String keys = nv.getNvKeys();
            try {
                java.util.Optional<NguyenVongXetTuyen> exist = nguyenVongRepository.findByNvKeys(keys);
                if (exist.isPresent()) {
                    NguyenVongXetTuyen e = exist.get();
                    // update fields
                    e.setNvMaNganh(nv.getNvMaNganh());
                    e.setMaToHop(nv.getMaToHop());
                    e.setNvThuTu(nv.getNvThuTu());
                    e.setDiemThxt(nv.getDiemThxt());
                    e.setDiemUtqd(nv.getDiemUtqd());
                    e.setDiemCong(nv.getDiemCong());
                    e.setDiemXetTuyen(nv.getDiemXetTuyen());
                    e.setNvKetQua(nv.getNvKetQua());
                    e.setTtPhuongThuc(nv.getTtPhuongThuc());
                    e.setTtThm(nv.getTtThm());
                    e.setUpdatedAt(java.time.LocalDateTime.now());
                    nguyenVongRepository.update(e);
                    updated++;
                } else {
                    nguyenVongRepository.bulkInsert(java.util.Collections.singletonList(nv), 1);
                    added++;
                }
            } catch (Exception ex) {
                skipped++;
            }
        }

        summary.setTotalRows(total);
        summary.setNewCount(added);
        summary.setUpdatedCount(updated);
        summary.setSkippedCount(skipped);
        return summary;
    }

    /**
     * Chạy quá trình xét tuyển cho một ngành (Giữ nguyên thuật toán cũ của bạn)
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
            // SỬA LỖI: Không gọi nguyenVongRepository.update(nv) ở đây nữa để tránh thắt nút cổ chai DB (N+1 query)
        }

        // TẬP TRUNG CẬP NHẬT THEO LÔ (BATCH UPDATE): Lưu lại tất cả thay đổi sau khi kết thúc vòng lặp
        for (NguyenVongXetTuyen nv : nguyenVongs) {
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

    @Transactional
    public Map<String, Object> rebuildAdmissionResults() {
        List<NguyenVongXetTuyen> aspirations = nguyenVongRepository.findAll();
        Map<String, Object> summary = new LinkedHashMap<>();

        if (aspirations.isEmpty()) {
            summary.put("totalAspirationRows", 0);
            summary.put("admittedCount", 0);
            summary.put("message", "Không có dữ liệu nguyện vọng để xét tuyển.");
            return summary;
        }

        Map<String, XtNganhEntity> majorsByCode = nganhRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getMaNganh() != null && !item.getMaNganh().isBlank())
                .collect(Collectors.toMap(
                        item -> item.getMaNganh().trim(),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, List<Combination>> combinationsByMajor = combinationRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getMaNganh() != null && !item.getMaNganh().isBlank())
                .collect(Collectors.groupingBy(
                        item -> item.getMaNganh().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<String, XtDiemThiXetTuyenEntity> scoresByCccd = diemThiRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getCccd() != null && !item.getCccd().isBlank())
                .collect(Collectors.toMap(
                        item -> item.getCccd().trim(),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, DiemCongXetTuyen> bonusByKey = diemCongRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getTsCccd() != null && !item.getTsCccd().isBlank())
                .filter(item -> item.getMaNganh() != null && !item.getMaNganh().isBlank())
                .collect(Collectors.toMap(
                        item -> bonusKey(item.getTsCccd(), item.getMaNganh(), item.getMaToHop()),
                        item -> item,
                        (left, right) -> preferBonus(left, right),
                        LinkedHashMap::new));

        Set<String> directAdmissionKeys = new LinkedHashSet<>();
        Set<String> directAdmissionCandidates = new LinkedHashSet<>();
        Map<String, Integer> reservedQuotaByMajor = new HashMap<>();

        for (NguyenVongXetTuyen nv : aspirations) {
            normalizeAspirationForRebuild(nv);
            if (!isDirectAdmissionRow(nv)) {
                continue;
            }
            directAdmissionKeys.add(nv.getNvKeys());
            directAdmissionCandidates.add(nv.getNnCccd());
            reservedQuotaByMajor.merge(nv.getNvMaNganh(), 1, Integer::sum);
            nv.setNvKetQua("Trúng tuyển (tuyển thẳng)");
            nv.setTtPhuongThuc("Tuyển thẳng");
            if (nv.getDiemThxt() == null) {
                nv.setDiemThxt(BigDecimal.ZERO);
            }
            if (nv.getDiemUtqd() == null) {
                nv.setDiemUtqd(BigDecimal.ZERO);
            }
            if (nv.getDiemCong() == null) {
                nv.setDiemCong(BigDecimal.ZERO);
            }
            if (nv.getDiemXetTuyen() == null) {
                nv.setDiemXetTuyen(nv.getDiemThxt().add(nv.getDiemUtqd()).add(nv.getDiemCong()));
            }
            nv.setUpdatedAt(LocalDateTime.now());
        }

        List<ScoredAspiration> eligibleRows = new ArrayList<>();

        for (NguyenVongXetTuyen nv : aspirations) {
            if (directAdmissionKeys.contains(nv.getNvKeys())) {
                continue;
            }

            XtNganhEntity major = majorsByCode.get(safeTrim(nv.getNvMaNganh()));
            XtDiemThiXetTuyenEntity score = scoresByCccd.get(safeTrim(nv.getNnCccd()));
            List<Combination> combinations = combinationsByMajor.getOrDefault(safeTrim(nv.getNvMaNganh()), List.of());

            if (major == null || score == null || combinations.isEmpty()) {
                nv.setDiemThxt(BigDecimal.ZERO);
                nv.setDiemUtqd(BigDecimal.ZERO);
                nv.setDiemCong(BigDecimal.ZERO);
                nv.setDiemXetTuyen(BigDecimal.ZERO);
                nv.setNvKetQua("Trượt");
                nv.setUpdatedAt(LocalDateTime.now());
                continue;
            }

            BestMethod bestMethod = resolveBestMethod(score);
            if (bestMethod.score() == null || bestMethod.score().compareTo(BigDecimal.ZERO) <= 0) {
                nv.setDiemThxt(BigDecimal.ZERO);
                nv.setDiemUtqd(BigDecimal.ZERO);
                nv.setDiemCong(BigDecimal.ZERO);
                nv.setDiemXetTuyen(BigDecimal.ZERO);
                nv.setNvKetQua("Trượt");
                nv.setUpdatedAt(LocalDateTime.now());
                continue;
            }

            CombinationSelection combinationSelection = selectBestCombination(bestMethod.method(), score, combinations, major);
            if (combinationSelection == null) {
                nv.setDiemThxt(BigDecimal.ZERO);
                nv.setDiemUtqd(BigDecimal.ZERO);
                nv.setDiemCong(BigDecimal.ZERO);
                nv.setDiemXetTuyen(BigDecimal.ZERO);
                nv.setNvKetQua("Trượt");
                nv.setUpdatedAt(LocalDateTime.now());
                continue;
            }

            DiemCongXetTuyen bonus = bonusByKey.get(bonusKey(nv.getNnCccd(), nv.getNvMaNganh(), combinationSelection.combination().getMaToHop()));
            BigDecimal diemCong = round2(bonus != null && bonus.getDiemCc() != null ? bonus.getDiemCc() : BigDecimal.ZERO);
            BigDecimal diemUtqd = round2(bonus != null && bonus.getDiemUtxt() != null ? bonus.getDiemUtxt() : BigDecimal.ZERO);
            BigDecimal diemThxt = round2(combinationSelection.adjustedScore());
            BigDecimal diemXetTuyen = round2(diemThxt.add(diemCong).add(diemUtqd));

            nv.setMaToHop(combinationSelection.combination().getMaToHop());
            nv.setNvKeys(generateNvKeys(nv.getNnCccd(), nv.getNvMaNganh(), nv.getMaToHop(), nv.getNvThuTu()));
            nv.setTtThm(combinationSelection.combination().getMaToHop());
            nv.setTtPhuongThuc(displayMethod(bestMethod.method()));
            nv.setDiemThxt(diemThxt);
            nv.setDiemCong(diemCong);
            nv.setDiemUtqd(diemUtqd);
            nv.setDiemXetTuyen(diemXetTuyen);
            nv.setNvKetQua("Trượt");
            nv.setUpdatedAt(LocalDateTime.now());

            if (diemXetTuyen.compareTo(defaultBigDecimal(major.getDiemSan())) >= 0) {
                eligibleRows.add(new ScoredAspiration(nv, major, diemXetTuyen));
            }
        }

        Map<String, Integer> regularQuotaByMajor = new HashMap<>();
        for (XtNganhEntity major : majorsByCode.values()) {
            int chiTieu = major.getChiTieu() == null ? 0 : major.getChiTieu();
            int reserved = reservedQuotaByMajor.getOrDefault(major.getMaNganh(), 0);
            regularQuotaByMajor.put(major.getMaNganh(), Math.max(0, chiTieu - reserved));
        }

        Map<String, List<ScoredAspiration>> candidatesByMajor = eligibleRows.stream()
                .collect(Collectors.groupingBy(
                        row -> row.aspiration().getNvMaNganh(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                                .sorted(scoredAspirationComparator())
                                .toList())));

        Map<String, ScoredAspiration> selectedByCandidate = new HashMap<>();
        Map<String, Set<String>> selectedKeysByMajor = new HashMap<>();
        Map<String, Integer> majorCursor = new HashMap<>();

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<ScoredAspiration>> entry : candidatesByMajor.entrySet()) {
                String majorCode = entry.getKey();
                int quota = regularQuotaByMajor.getOrDefault(majorCode, 0);
                if (quota <= 0) {
                    continue;
                }

                Set<String> selectedKeys = selectedKeysByMajor.computeIfAbsent(majorCode, key -> new LinkedHashSet<>());
                while (selectedKeys.size() < quota) {
                    int cursor = majorCursor.getOrDefault(majorCode, 0);
                    if (cursor >= entry.getValue().size()) {
                        break;
                    }

                    ScoredAspiration candidate = entry.getValue().get(cursor);
                    majorCursor.put(majorCode, cursor + 1);

                    String cccd = candidate.aspiration().getNnCccd();
                    if (directAdmissionCandidates.contains(cccd)) {
                        continue;
                    }

                    ScoredAspiration current = selectedByCandidate.get(cccd);
                    if (current == null) {
                        selectedByCandidate.put(cccd, candidate);
                        selectedKeys.add(candidate.aspiration().getNvKeys());
                        changed = true;
                        continue;
                    }

                    if (candidate.aspiration().getNvThuTu() < current.aspiration().getNvThuTu()) {
                        Set<String> previousMajorKeys = selectedKeysByMajor.get(current.aspiration().getNvMaNganh());
                        if (previousMajorKeys != null) {
                            previousMajorKeys.remove(current.aspiration().getNvKeys());
                        }
                        selectedByCandidate.put(cccd, candidate);
                        selectedKeys.add(candidate.aspiration().getNvKeys());
                        changed = true;
                    }
                }
            }
        } while (changed);

        Set<String> admittedKeys = selectedByCandidate.values().stream()
                .map(item -> item.aspiration().getNvKeys())
                .collect(Collectors.toSet());

        int admittedCount = 0;
        for (NguyenVongXetTuyen nv : aspirations) {
            if (directAdmissionKeys.contains(nv.getNvKeys())) {
                admittedCount++;
                continue;
            }
            if (admittedKeys.contains(nv.getNvKeys())) {
                nv.setNvKetQua("Trúng tuyển");
                admittedCount++;
            } else if (selectedByCandidate.containsKey(nv.getNnCccd())
                    && nv.getNvThuTu() > selectedByCandidate.get(nv.getNnCccd()).aspiration().getNvThuTu()) {
                nv.setNvKetQua("Bỏ (Đã đậu NV trước)");
            } else {
                nv.setNvKetQua("Trượt");
            }
            nv.setUpdatedAt(LocalDateTime.now());
        }

        nguyenVongRepository.updateBatch(aspirations);

        summary.put("totalAspirationRows", aspirations.size());
        summary.put("directAdmissionCount", directAdmissionKeys.size());
        summary.put("eligibleRegularRows", eligibleRows.size());
        summary.put("admittedCount", admittedCount);
        summary.put("message", "Đã tính lại toàn bộ kết quả xét tuyển từ dữ liệu hiện có.");
        return summary;
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
        List<DiemCongXetTuyen> diemCongs = diemCongRepository.findByTsCccdAndMaNganhAndMaToHop(tsCccd, maNganh, maToHop);

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

    private void normalizeAspirationForRebuild(NguyenVongXetTuyen nv) {
        nv.setNnCccd(safeTrim(nv.getNnCccd()));
        nv.setNvMaNganh(safeTrim(nv.getNvMaNganh()));
        nv.setMaToHop(safeTrim(defaultIfBlank(nv.getMaToHop(), nv.getTtThm())));
        nv.setTtThm(safeTrim(defaultIfBlank(nv.getTtThm(), nv.getMaToHop())));
        if (nv.getNvKeys() == null || nv.getNvKeys().isBlank()) {
            nv.setNvKeys(generateNvKeys(nv.getNnCccd(), nv.getNvMaNganh(), nv.getMaToHop(), nv.getNvThuTu()));
        }
    }

    private boolean isDirectAdmissionRow(NguyenVongXetTuyen nv) {
        String result = normalizeAscii(nv.getNvKetQua());
        String method = normalizeAscii(nv.getTtPhuongThuc());
        return method.contains("tuyen thang")
                || method.contains("xtt")
                || result.contains("tuyen thang");
    }

    private BestMethod resolveBestMethod(XtDiemThiXetTuyenEntity score) {
        List<BestMethod> methods = new ArrayList<>();
        methods.add(new BestMethod("THPT", safeScore(score.getThptEquivalentScore())));
        methods.add(new BestMethod("VSAT", safeScore(score.getVsatEquivalentScore())));
        methods.add(new BestMethod("DGNL", safeScore(score.getDgnlEquivalentScore())));
        return methods.stream()
                .max(Comparator.comparing(BestMethod::score, Comparator.nullsLast(BigDecimal::compareTo)))
                .orElse(new BestMethod("THPT", BigDecimal.ZERO));
    }

    private CombinationSelection selectBestCombination(String method,
                                                       XtDiemThiXetTuyenEntity score,
                                                       List<Combination> combinations,
                                                       XtNganhEntity major) {
        CombinationSelection best = null;
        for (Combination combination : combinations) {
            BigDecimal baseScore = switch (safeTrim(method).toUpperCase(Locale.ROOT)) {
                case "DGNL" -> calculateDgnlCombinationScore(score, combination, major);
                case "VSAT" -> calculateVsatCombinationScore(score, combination);
                default -> calculateThptCombinationScore(score, combination);
            };
            if (baseScore == null || baseScore.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal adjustedScore = round2(baseScore.add(defaultBigDecimal(combination.getDoLech())));
            CombinationSelection current = new CombinationSelection(combination, baseScore, adjustedScore);
            if (best == null
                    || current.baseScore().compareTo(best.baseScore()) > 0
                    || (current.baseScore().compareTo(best.baseScore()) == 0
                    && current.adjustedScore().compareTo(best.adjustedScore()) > 0)) {
                best = current;
            }
        }
        return best;
    }

    private BigDecimal calculateDgnlCombinationScore(XtDiemThiXetTuyenEntity score,
                                                     Combination combination,
                                                     XtNganhEntity major) {
        if (score.getNl1() == null || score.getNl1().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String targetToHop = safeTrim(combination.getMaToHop());
        if ("A00".equalsIgnoreCase(targetToHop)) {
            targetToHop = "A01";
        } else if ("C00".equalsIgnoreCase(targetToHop)) {
            targetToHop = "C01";
        } else if (targetToHop.isBlank()) {
            targetToHop = safeTrim(major.getToHopGoc());
        }

        try {
            Map<String, String> result = bangQuyDoiService.quyDoiDiemKhaoThi("DGNL", targetToHop, score.getNl1());
            String value = result == null ? null : result.get("diemQuyDoi");
            return value == null || value.isBlank() ? null : round2(new BigDecimal(value));
        } catch (Exception ignored) {
            return score.getNl1().divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal calculateVsatCombinationScore(XtDiemThiXetTuyenEntity score, Combination combination) {
        return calculateSubjectCombinationScore(score, combination, true);
    }

    private BigDecimal calculateThptCombinationScore(XtDiemThiXetTuyenEntity score, Combination combination) {
        return calculateSubjectCombinationScore(score, combination, false);
    }

    private BigDecimal calculateSubjectCombinationScore(XtDiemThiXetTuyenEntity score,
                                                        Combination combination,
                                                        boolean convertVsat) {
        BigDecimal mon1 = resolveCombinationSubjectScore(score, combination.getThMon1(), convertVsat);
        BigDecimal mon2 = resolveCombinationSubjectScore(score, combination.getThMon2(), convertVsat);
        BigDecimal mon3 = resolveCombinationSubjectScore(score, combination.getThMon3(), convertVsat);
        if (mon1 == null || mon2 == null || mon3 == null) {
            return null;
        }

        BigDecimal hs1 = BigDecimal.valueOf(combination.getHsMon1() == null ? 1 : combination.getHsMon1());
        BigDecimal hs2 = BigDecimal.valueOf(combination.getHsMon2() == null ? 1 : combination.getHsMon2());
        BigDecimal hs3 = BigDecimal.valueOf(combination.getHsMon3() == null ? 1 : combination.getHsMon3());

        BigDecimal weighted = mon1.multiply(hs1).add(mon2.multiply(hs2)).add(mon3.multiply(hs3));
        BigDecimal totalWeight = hs1.add(hs2).add(hs3);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return weighted.divide(totalWeight, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("3")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveCombinationSubjectScore(XtDiemThiXetTuyenEntity score, String subjectCode, boolean convertVsat) {
        String normalized = safeTrim(subjectCode).toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return null;
        }

        BigDecimal raw = switch (normalized) {
            case "TO", "TOAN" -> score.getTo();
            case "LI", "VAT_LY" -> score.getLi();
            case "HO", "HOA_HOC" -> score.getHo();
            case "SI", "SINH_HOC" -> score.getSi();
            case "SU", "LICH_SU" -> score.getSu();
            case "DI", "DIA_LY" -> score.getDi();
            case "VA", "NGU_VAN" -> score.getVa();
            case "N1", "N1_THI", "TIENG_ANH" -> firstPositive(score.getN1Cc(), score.getN1Thi());
            case "TI" -> score.getTi();
            case "KTPL" -> score.getKtpl();
            case "CNCN" -> score.getCncn();
            case "CNNN" -> score.getCnnn();
            default -> null;
        };

        if (raw == null || raw.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (!convertVsat) {
            return round2(raw);
        }

        String lookupCode = switch (normalized) {
            case "TO", "TOAN" -> "TOAN";
            case "LI", "VAT_LY" -> "VAT_LY";
            case "HO", "HOA_HOC" -> "HOA_HOC";
            case "SI", "SINH_HOC" -> "SINH_HOC";
            case "SU", "LICH_SU" -> "LICH_SU";
            case "DI", "DIA_LY" -> "DIA_LY";
            case "VA", "NGU_VAN" -> "NGU_VAN";
            case "N1", "N1_THI", "TIENG_ANH" -> "TIENG_ANH";
            case "TI" -> "TI";
            case "KTPL" -> "KTPL";
            case "CNCN" -> "CNCN";
            case "CNNN" -> "CNNN";
            default -> normalized;
        };

        try {
            Map<String, String> result = bangQuyDoiService.quyDoiDiemKhaoThi("VSAT", lookupCode, raw);
            String value = result == null ? null : result.get("diemQuyDoi");
            return value == null || value.isBlank() ? null : round2(new BigDecimal(value));
        } catch (Exception ignored) {
            return raw.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
        }
    }

    private Comparator<ScoredAspiration> scoredAspirationComparator() {
        return Comparator
                .comparing(ScoredAspiration::score, Comparator.reverseOrder())
                .thenComparing(item -> item.aspiration().getNvThuTu(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(item -> item.aspiration().getNnCccd(), Comparator.nullsLast(String::compareTo));
    }

    private DiemCongXetTuyen preferBonus(DiemCongXetTuyen left, DiemCongXetTuyen right) {
        BigDecimal leftTotal = left == null ? BigDecimal.ZERO : defaultBigDecimal(left.getDiemTong());
        BigDecimal rightTotal = right == null ? BigDecimal.ZERO : defaultBigDecimal(right.getDiemTong());
        return rightTotal.compareTo(leftTotal) >= 0 ? right : left;
    }

    private String bonusKey(String cccd, String majorCode, String toHop) {
        return safeTrim(cccd) + "|" + safeTrim(majorCode) + "|" + safeTrim(toHop);
    }

    private BigDecimal firstPositive(BigDecimal first, BigDecimal second) {
        if (first != null && first.compareTo(BigDecimal.ZERO) > 0) {
            return first;
        }
        return second;
    }

    private BigDecimal safeScore(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal round2(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String displayMethod(String method) {
        return switch (safeTrim(method).toUpperCase(Locale.ROOT)) {
            case "DGNL" -> "DGNL";
            case "VSAT" -> "V-SAT";
            case "THPT" -> "THPT";
            default -> safeTrim(method);
        };
    }

    private String defaultIfBlank(String value, String fallback) {
        return safeTrim(value).isBlank() ? safeTrim(fallback) : safeTrim(value);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeAscii(String value) {
        return java.text.Normalizer.normalize(safeTrim(value), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    private record BestMethod(String method, BigDecimal score) {
    }

    private record CombinationSelection(Combination combination, BigDecimal baseScore, BigDecimal adjustedScore) {
    }

    private record ScoredAspiration(NguyenVongXetTuyen aspiration, XtNganhEntity major, BigDecimal score) {
    }
}
