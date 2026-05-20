package com.example.admissions_management.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.admissions_management.application.dto.response.CandidateAspirationResult;
import com.example.admissions_management.application.service.candidate.CandidateLookupResult;
import com.example.admissions_management.application.service.candidate.CombinationScoreResult;
import com.example.admissions_management.application.service.candidate.CombinationSpec;
import com.example.admissions_management.application.service.candidate.DgnlAspirationResult;
import com.example.admissions_management.application.service.candidate.DgnlCalculationResult;
import com.example.admissions_management.application.service.candidate.OptionItem;
import com.example.admissions_management.application.service.candidate.VsatThptCalculationResult;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNguyenVongXetTuyenRepository;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import com.example.admissions_management.presentation.web.model.DgnlCalculatorForm;
import com.example.admissions_management.presentation.web.model.VsatThptCalculatorForm;

@Service
public class CandidatePortalService {

    private static final BigDecimal DGNL_MAX_SCORE = new BigDecimal("1200");
    private static final BigDecimal VSAT_MAX_SCORE = new BigDecimal("150");
    private static final BigDecimal THPT_MAX_SCORE = new BigDecimal("10");
    private static final BigDecimal BONUS_MAX_SCORE = new BigDecimal("3");
    private static final BigDecimal VSAT_TO_10_DIVISOR = new BigDecimal("15");

    private final CandidateRepository candidateRepository;
    private final SpringDataXtNguyenVongXetTuyenRepository aspirationRepository;
    private final MajorRepository majorRepository;
    private final BangQuyDoiService bangQuyDoiService;

    // Các bản đồ hỗ trợ tính toán nhanh
    private final Map<String, BigDecimal> priorityObjectPoint;
    private final Map<String, BigDecimal> priorityRegionPoint;
    private final Map<String, String> subjectLabelByCode;

    public CandidatePortalService(CandidateRepository candidateRepository,
                                  SpringDataXtNguyenVongXetTuyenRepository aspirationRepository,
                                  MajorRepository majorRepository,
                                  BangQuyDoiService bangQuyDoiService) {
        this.candidateRepository = candidateRepository;
        this.aspirationRepository = aspirationRepository;
        this.majorRepository = majorRepository;
        this.bangQuyDoiService = bangQuyDoiService;
        
        this.priorityObjectPoint = createPriorityObjectPointMap();
        this.priorityRegionPoint = createPriorityRegionPointMap();
        this.subjectLabelByCode = createSubjectLabelMap();
    }

    // --- CÁC PHƯƠNG THỨC LẤY OPTION CHO GIAO DIỆN ---

    public List<OptionItem> getMajorOptions() {
        List<OptionItem> options = new ArrayList<>();
        List<XtNganhEntity> majors = majorRepository.findAll();
        for (XtNganhEntity m : majors) {
            options.add(new OptionItem(m.getMaNganh(), m.getMaNganh() + " - " + m.getTenNganh()));
        }
        return options;
    }

    public List<OptionItem> getPriorityObjectOptions() {
        return List.of(
                new OptionItem("NONE", "Không ưu tiên (0.00)"),
                new OptionItem("UT1", "Đối tượng ưu tiên 1 (+2.00)"),
                new OptionItem("UT2", "Đối tượng ưu tiên 2 (+1.00)"),
                new OptionItem("UT3", "Đối tượng ưu tiên 3 (+0.50)"));
    }

    public List<OptionItem> getPriorityRegionOptions() {
        return List.of(
                new OptionItem("KV3", "Khu vực 3 (0.00)"),
                new OptionItem("KV2", "Khu vực 2 (+0.25)"),
                new OptionItem("KV2NT", "Khu vực 2-NT (+0.50)"),
                new OptionItem("KV1", "Khu vực 1 (+0.75)"));
    }

    public List<OptionItem> getBonusSubjectOptions() {
        return List.of(
                new OptionItem("NONE", "Không cộng"),
                new OptionItem("TOAN", "Toán"),
                new OptionItem("VAT_LY", "Vật lý"),
                new OptionItem("HOA_HOC", "Hóa học"),
                new OptionItem("NGU_VAN", "Ngữ văn"),
                new OptionItem("TIENG_ANH", "Tiếng Anh"),
                new OptionItem("SINH_HOC", "Sinh học"),
                new OptionItem("LICH_SU", "Lịch sử"),
                new OptionItem("DIA_LY", "Địa lý"),
                new OptionItem("GDCD", "GDCD"));
    }

    // --- CHỨC NĂNG TRA CỨU KẾT QUẢ (DÙNG DATABASE) ---

    public CandidateLookupResult lookupResult(CandidateLookupForm form) {
        CandidateLookupResult result = new CandidateLookupResult();
        result.setSearched(true);

        String username = normalizeCandidateCode(form.getUsername());
        String inputBirthDate = normalizeBirthDate(form.getPassword());

        if (username.isBlank() || inputBirthDate.isBlank()) {
            result.setFound(false);
            result.setMessage("Vui lòng nhập đầy đủ mã thí sinh và ngày sinh.");
            return result;
        }

        Optional<XtThiSinhXetTuyen25Entity> candidateOptional = candidateRepository.findByCccd(username);
        if (candidateOptional.isEmpty()) {
            result.setFound(false);
            result.setMessage("Không tìm thấy thí sinh với mã đã nhập.");
            return result;
        }

        XtThiSinhXetTuyen25Entity candidate = candidateOptional.get();
        String storedBirthDate = normalizeBirthDate(candidate.getNgaySinh());
        if (!inputBirthDate.equals(storedBirthDate)) {
            result.setFound(false);
            result.setMessage("Ngày sinh không khớp với thông tin thí sinh.");
            return result;
        }

        result.setFound(true);
        result.setCccd(candidate.getCccd());
        result.setFullName(candidate.getHo() + " " + candidate.getTen());

        List<XtNguyenVongXetTuyenEntity> aspirations = aspirationRepository.findByNnCccdOrderByNvThuTuAsc(username);
        if (aspirations == null || aspirations.isEmpty()) {
            result.setAspirationResults(new ArrayList<>());
            result.setAdmitted(false);
            result.setMessage("Thí sinh chưa có nguyện vọng xét tuyển.");
            return result;
        }

        boolean admittedBefore = false;
        List<CandidateAspirationResult> aspirationResults = new ArrayList<>();

        for (XtNguyenVongXetTuyenEntity asp : aspirations) {
            CandidateAspirationResult ar = new CandidateAspirationResult();
            ar.setMajorCode(asp.getNvMaNganh());
            ar.setMajorName(majorRepository.findByMaNganh(asp.getNvMaNganh())
                    .map(XtNganhEntity::getTenNganh)
                    .orElse(asp.getNvMaNganh()));
            ar.setScore(round(asp.getDiemXetTuyen()));
            ar.setCombination(asp.getTtThm());
            ar.setMethod(asp.getTtPhuongThuc());
            ar.setNvThuTu(asp.getNvThuTu());

            if (admittedBefore) {
                ar.setAdmitted(false);
                ar.setResultNote("Bỏ (Đã đậu NV trước)");
            } else if (isAdmittedStatus(asp.getNvKetQua())) {
                ar.setAdmitted(true);
                ar.setResultNote("Trúng tuyển");
                admittedBefore = true;
            } else {
                ar.setAdmitted(false);
                ar.setResultNote("Không trúng tuyển");
            }
            aspirationResults.add(ar);
        }

        result.setAspirationResults(aspirationResults);
        result.setAdmitted(admittedBefore);
        result.setMessage(admittedBefore ? "Đã tìm thấy kết quả: Trúng tuyển" : "Đã tìm thấy kết quả: Chưa trúng tuyển");

        return result;
    }

    // --- CHỨC NĂNG TÍNH TOÁN DGNL (LOGIC CŨ + DB) ---

    public DgnlCalculationResult calculateDgnl(DgnlCalculatorForm form) {
        DgnlCalculationResult result = new DgnlCalculationResult();
        result.setCalculated(true);

        Optional<XtNganhEntity> nganhOpt = majorRepository.findByMaNganh(form.getMajorCode());
        if (nganhOpt.isEmpty()) {
            result.setMessage("Ngành không tồn tại.");
            return result;
        }
        XtNganhEntity nganh = nganhOpt.get();

        BigDecimal originalScore = clamp(nonNull(form.getDgnlScore()), BigDecimal.ZERO, DGNL_MAX_SCORE);
        String toHop = nganh.getToHopGoc();
        
        // Logic chuẩn hóa tổ hợp cho DGNL
        if ("A00".equals(toHop)) toHop = "A01";
        if ("C00".equals(toHop)) toHop = "C01";

        BigDecimal converted30;
        try {
            Map<String, String> quyDoiMap = bangQuyDoiService.quyDoiDiemKhaoThi("DGNL", toHop, originalScore);
            converted30 = new BigDecimal(quyDoiMap.get("diemQuyDoi"));
        } catch (Exception e) {
            converted30 = BigDecimal.ZERO;
        }

        BigDecimal priorityScore = calculatePriorityScore(form.getPriorityObjectCode(), form.getPriorityRegionCode());
        BigDecimal bonusScore = round(clamp(nonNull(form.getBonusPoint()), BigDecimal.ZERO, BONUS_MAX_SCORE));
        BigDecimal totalScore = round(converted30.add(priorityScore).add(bonusScore));

        DgnlAspirationResult ar = new DgnlAspirationResult();
        ar.setAspirationName("NV Dự tính");
        ar.setMajorName(nganh.getTenNganh());
        ar.setOriginalCombination(toHop);
        ar.setConvertedScore(converted30);
        ar.setPriorityScore(priorityScore);
        ar.setBonusScore(bonusScore);
        ar.setTotalScore(totalScore);
        ar.setThresholdScore(nganh.getDiemSan());
        ar.setAdmissionScore(nganh.getDiemTrungTuyen());
        ar.setPassThreshold(totalScore.compareTo(nonNull(nganh.getDiemSan())) >= 0);
        ar.setPassAdmission(nganh.getDiemTrungTuyen() != null && totalScore.compareTo(nganh.getDiemTrungTuyen()) >= 0);

        result.setMajorName(nganh.getTenNganh());
        result.setRows(List.of(ar));
        result.setMessage("Đã tính điểm DGNL dự tính.");
        return result;
    }

    // --- CHỨC NĂNG TÍNH TOÁN VSAT/THPT ---

    public VsatThptCalculationResult calculateVsatThpt(VsatThptCalculatorForm form) {
        VsatThptCalculationResult result = new VsatThptCalculationResult();
        result.setCalculated(true);

        Optional<XtNganhEntity> nganhOpt = majorRepository.findByMaNganh(form.getMajorCode());
        if (nganhOpt.isEmpty()) {
            result.setMessage("Ngành không tồn tại.");
            return result;
        }
        XtNganhEntity nganh = nganhOpt.get();

        boolean isVsat = "VSAT".equalsIgnoreCase(form.getMethodType());
        Map<String, BigDecimal> subjectScores = normalizeSubjectScores(form, isVsat);

        // Ưu tiên điểm quy đổi tiếng Anh nếu cao hơn điểm thi
        BigDecimal englishConverted = round(clamp(nonNull(form.getEnglishConvertedScore()), BigDecimal.ZERO, THPT_MAX_SCORE));
        if (englishConverted.compareTo(subjectScores.get("TIENG_ANH")) > 0) {
            subjectScores.put("TIENG_ANH", englishConverted);
        }

        BigDecimal priorityScore = calculatePriorityScore(form.getPriorityObjectCode(), form.getPriorityRegionCode());
        BigDecimal bonusScore = round(clamp(nonNull(form.getBonusPoint()), BigDecimal.ZERO, BONUS_MAX_SCORE));

        // Giả lập danh sách tổ hợp dựa trên tổ hợp gốc của ngành (Vì DB hiện tại chỉ lưu 1 tổ hợp gốc)
        List<CombinationSpec> specs = getSpecsFromCode(nganh.getToHopGoc());
        List<CombinationScoreResult> combinationResults = new ArrayList<>();

        for (CombinationSpec combination : specs) {
            BigDecimal total = subjectScores.get(combination.subjectCode1())
                    .add(subjectScores.get(combination.subjectCode2()))
                    .add(subjectScores.get(combination.subjectCode3()))
                    .add(priorityScore);

            if (!"NONE".equalsIgnoreCase(form.getBonusSubjectCode()) && combination.containsSubject(form.getBonusSubjectCode())) {
                total = total.add(bonusScore);
            }

            CombinationScoreResult cr = new CombinationScoreResult();
            cr.setCombinationCode(combination.code());
            cr.setSubjectFormula(subjectLabelByCode.get(combination.subjectCode1()) + " + " + subjectLabelByCode.get(combination.subjectCode2()) + " + " + subjectLabelByCode.get(combination.subjectCode3()));
            cr.setTotalScore(round(total));
            cr.setThresholdScore(nganh.getDiemSan());
            cr.setAdmissionScore(nganh.getDiemTrungTuyen());
            cr.setPassThreshold(total.compareTo(nonNull(nganh.getDiemSan())) >= 0);
            cr.setPassAdmission(nganh.getDiemTrungTuyen() != null && total.compareTo(nganh.getDiemTrungTuyen()) >= 0);
            combinationResults.add(cr);
        }

        result.setMethodType(isVsat ? "VSAT" : "THPT");
        result.setMajorName(nganh.getTenNganh());
        result.setPriorityScore(priorityScore);
        result.setBonusScore(bonusScore);
        result.setCombinationResults(combinationResults);
        result.setMessage("Đã tính điểm xét tuyển theo tổ hợp môn.");
        return result;
    }

    // --- HELPER METHODS ---

    private boolean isAdmittedStatus(String status) {
        if (status == null) return false;
        String val = status.toLowerCase().trim();
        return val.contains("trung tuyen") || val.contains("dat") || val.equals("1") || val.equals("true");
    }

    private String normalizeCandidateCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeBirthDate(String value) {
        if (value == null) return "";
        String digits = value.trim().replaceAll("[^0-9]", "");
        if (digits.length() != 8) return digits;
        // ddMMyyyy
        return digits.substring(0, 2) + digits.substring(2, 4) + digits.substring(4, 8);
    }

    private Map<String, BigDecimal> normalizeSubjectScores(VsatThptCalculatorForm form, boolean isVsat) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("TOAN", normalizeSubject(form.getMathScore(), isVsat));
        map.put("NGU_VAN", normalizeSubject(form.getLiteratureScore(), isVsat));
        map.put("TIENG_ANH", normalizeSubject(form.getEnglishScore(), isVsat));
        map.put("VAT_LY", normalizeSubject(form.getPhysicsScore(), isVsat));
        map.put("HOA_HOC", normalizeSubject(form.getChemistryScore(), isVsat));
        map.put("SINH_HOC", normalizeSubject(form.getBiologyScore(), isVsat));
        map.put("LICH_SU", normalizeSubject(form.getHistoryScore(), isVsat));
        map.put("DIA_LY", normalizeSubject(form.getGeographyScore(), isVsat));
        map.put("GDCD", normalizeSubject(form.getCivicEducationScore(), isVsat));
        return map;
    }

    private BigDecimal normalizeSubject(BigDecimal score, boolean isVsat) {
        BigDecimal val = nonNull(score);
        if (isVsat) {
            return val.divide(VSAT_TO_10_DIVISOR, 6, RoundingMode.HALF_UP);
        }
        return val;
    }

    private BigDecimal calculatePriorityScore(String obj, String reg) {
        BigDecimal p1 = priorityObjectPoint.getOrDefault(obj, BigDecimal.ZERO);
        BigDecimal p2 = priorityRegionPoint.getOrDefault(reg, BigDecimal.ZERO);
        return p1.add(p2);
    }

    private List<CombinationSpec> getSpecsFromCode(String code) {
        if (code == null) return List.of();
        return switch (code.toUpperCase()) {
            case "A01" -> List.of(new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"));
            case "B00" -> List.of(new CombinationSpec("B00", "TOAN", "HOA_HOC", "SINH_HOC"));
            case "C01" -> List.of(new CombinationSpec("C01", "TOAN", "NGU_VAN", "VAT_LY"));
            case "D01" -> List.of(new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"));
            default -> List.of(new CombinationSpec(code, "TOAN", "NGU_VAN", "TIENG_ANH"));
        };
    }

    private Map<String, BigDecimal> createPriorityObjectPointMap() {
        Map<String, BigDecimal> m = new HashMap<>();
        m.put("NONE", BigDecimal.ZERO); m.put("UT1", new BigDecimal("2.0"));
        m.put("UT2", new BigDecimal("1.0")); m.put("UT3", new BigDecimal("0.5"));
        return m;
    }

    private Map<String, BigDecimal> createPriorityRegionPointMap() {
        Map<String, BigDecimal> m = new HashMap<>();
        m.put("KV3", BigDecimal.ZERO); m.put("KV2", new BigDecimal("0.25"));
        m.put("KV2NT", new BigDecimal("0.5")); m.put("KV1", new BigDecimal("0.75"));
        return m;
    }

    private Map<String, String> createSubjectLabelMap() {
        Map<String, String> m = new HashMap<>();
        m.put("TOAN", "Toán"); m.put("NGU_VAN", "Văn"); m.put("TIENG_ANH", "Anh");
        m.put("VAT_LY", "Lý"); m.put("HOA_HOC", "Hóa"); m.put("SINH_HOC", "Sinh");
        m.put("LICH_SU", "Sử"); m.put("DIA_LY", "Địa"); m.put("GDCD", "GDCD");
        return m;
    }

    private BigDecimal nonNull(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal round(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal clamp(BigDecimal v, BigDecimal min, BigDecimal max) {
        if (v.compareTo(min) < 0) return min;
        if (v.compareTo(max) > 0) return max;
        return v;
    }
}