package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.response.CandidateAspirationResult;
import com.example.admissions_management.application.service.candidate.CandidateLookupResult;
import com.example.admissions_management.application.service.candidate.CombinationScoreResult;
import com.example.admissions_management.application.service.candidate.CombinationSpec;
import com.example.admissions_management.application.service.candidate.DgnlAspirationResult;
import com.example.admissions_management.application.service.candidate.DgnlCalculationResult;
import com.example.admissions_management.application.service.candidate.MajorConfig;
import com.example.admissions_management.application.service.candidate.OptionItem;
import com.example.admissions_management.application.service.candidate.VsatThptCalculationResult;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNguyenVongXetTuyenRepository;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import com.example.admissions_management.presentation.web.model.DgnlCalculatorForm;
import com.example.admissions_management.presentation.web.model.VsatThptCalculatorForm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    private final Map<String, MajorConfig> majorByCode;
    private final Map<String, BigDecimal> priorityObjectPoint;
    private final Map<String, BigDecimal> priorityRegionPoint;
    private final Map<String, String> subjectLabelByCode;

    public CandidatePortalService(CandidateRepository candidateRepository,
            SpringDataXtNguyenVongXetTuyenRepository aspirationRepository,
            MajorRepository majorRepository) {
        this.candidateRepository = candidateRepository;
        this.aspirationRepository = aspirationRepository;
        this.majorRepository = majorRepository;
        this.majorByCode = createMajors();
        this.priorityObjectPoint = createPriorityObjectPointMap();
        this.priorityRegionPoint = createPriorityRegionPointMap();
        this.subjectLabelByCode = createSubjectLabelMap();
    }

    public List<OptionItem> getMajorOptions() {
        List<OptionItem> options = new ArrayList<>();

        majorRepository.findAll().stream()
                .sorted((left, right) -> left.getMaNganh().compareToIgnoreCase(right.getMaNganh()))
                .forEach(major -> options.add(new OptionItem(
                        major.getMaNganh(),
                        major.getMaNganh() + " - " + major.getTenNganh())));

        if (!options.isEmpty()) {
            return options;
        }

        for (MajorConfig major : majorByCode.values()) {
            options.add(new OptionItem(major.code(), major.code() + " - " + major.name()));
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

    public CandidateLookupResult lookupResult(CandidateLookupForm form) {
        CandidateLookupResult result = new CandidateLookupResult();
        result.setSearched(true);

        String username = normalizeCandidateCode(form.getUsername());
        String inputBirthDate = normalizeBirthDate(form.getPassword());

        if (username.isBlank() || inputBirthDate.isBlank()) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Vui lòng nhập đầy đủ CCCD và ngày sinh.");
            return result;
        }

        Optional<XtThiSinhXetTuyen25Entity> candidateOptional = candidateRepository.findByCccd(username);
        if (candidateOptional.isEmpty()) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Không tìm thấy thí sinh với CCCD đã nhập.");
            return result;
        }

        XtThiSinhXetTuyen25Entity candidate = candidateOptional.get();
        String storedBirthDate = normalizeBirthDate(candidate.getNgaySinh());
        if (!inputBirthDate.equals(storedBirthDate)) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Ngày sinh không khớp với thông tin thí sinh.");
            return result;
        }

        result.setFound(true);
        result.setCccd(candidate.getCccd());
        result.setFullName(buildFullName(candidate));

        List<XtNguyenVongXetTuyenEntity> aspirations =
                aspirationRepository.findByNnCccdOrderByNvThuTuAsc(username);

        if (aspirations == null || aspirations.isEmpty()) {
            result.setAspirationResults(new ArrayList<>());
            result.setAdmitted(false);
            result.setMessage("Thí sinh chưa có dữ liệu nguyện vọng xét tuyển.");
            return result;
        }

        boolean admittedBefore = false;
        List<CandidateAspirationResult> aspirationResults = new ArrayList<>();

        for (XtNguyenVongXetTuyenEntity aspiration : aspirations) {
            CandidateAspirationResult row = new CandidateAspirationResult();
            row.setMajorCode(aspiration.getNvMaNganh());
            row.setMajorName(majorRepository.findByMaNganh(aspiration.getNvMaNganh())
                    .map(major -> major.getTenNganh())
                    .orElse(aspiration.getNvMaNganh()));
            row.setScore(round(aspiration.getDiemXetTuyen()));
            row.setCombination(normalizeDisplayValue(aspiration.getTtThm(), "Chưa cập nhật"));
            row.setMethod(normalizeDisplayValue(aspiration.getTtPhuongThuc(), "Chưa cập nhật"));
            row.setNvThuTu(aspiration.getNvThuTu());

            if (admittedBefore) {
                row.setAdmitted(false);
                row.setResultNote("Không xét do đã trúng tuyển nguyện vọng trước");
            } else if (isAdmittedAspiration(aspiration)) {
                row.setAdmitted(true);
                row.setResultNote("Trúng tuyển");
                admittedBefore = true;
                result.setMajorName(row.getMajorName());
                result.setScore(row.getScore());
                result.setCombination(row.getCombination());
                result.setMethod(row.getMethod());
            } else {
                row.setAdmitted(false);
                row.setResultNote("Không trúng tuyển");
            }

            aspirationResults.add(row);
        }

        result.setAspirationResults(aspirationResults);
        result.setAdmitted(admittedBefore);
        result.setMessage(admittedBefore
                ? "Đã tìm thấy kết quả xét tuyển của thí sinh."
                : "Đã tìm thấy dữ liệu thí sinh, hiện chưa có nguyện vọng trúng tuyển.");

        return result;
    }

    public DgnlCalculationResult calculateDgnl(DgnlCalculatorForm form) {
        DgnlCalculationResult result = new DgnlCalculationResult();
        result.setCalculated(true);

        MajorConfig major = majorByCode.get(normalize(form.getMajorCode()));
        if (major == null) {
            result.setMessage("Vui lòng chọn ngành xét tuyển.");
            return result;
        }

        BigDecimal dgnlScore = clamp(nonNull(form.getDgnlScore()), BigDecimal.ZERO, DGNL_MAX_SCORE);
        BigDecimal converted30 = round(dgnlScore.divide(new BigDecimal("40"), 6, RoundingMode.HALF_UP));
        BigDecimal priorityScore = calculatePriorityScore(form.getPriorityObjectCode(), form.getPriorityRegionCode());
        BigDecimal bonusScore = round(clamp(nonNull(form.getBonusPoint()), BigDecimal.ZERO, BONUS_MAX_SCORE));
        BigDecimal totalScore = round(converted30.add(priorityScore).add(bonusScore));

        DgnlAspirationResult aspirationResult = new DgnlAspirationResult();
        aspirationResult.setAspirationName("NV1");
        aspirationResult.setMajorName(major.name());
        aspirationResult.setOriginalCombination(major.originalCombination());
        aspirationResult.setConvertedScore(converted30);
        aspirationResult.setPriorityScore(priorityScore);
        aspirationResult.setBonusScore(bonusScore);
        aspirationResult.setTotalScore(totalScore);
        aspirationResult.setThresholdScore(major.dgnlThreshold());
        aspirationResult.setAdmissionScore(major.dgnlAdmission());
        aspirationResult.setPassThreshold(totalScore.compareTo(major.dgnlThreshold()) >= 0);
        aspirationResult
                .setPassAdmission(major.dgnlAdmission() != null && totalScore.compareTo(major.dgnlAdmission()) >= 0);

        result.setMajorName(major.name());
        result.setRows(List.of(aspirationResult));
        result.setMessage("Đã hoàn tất tính điểm DGNL và đối chiếu với ngưỡng xét tuyển.");
        return result;
    }

    public VsatThptCalculationResult calculateVsatThpt(VsatThptCalculatorForm form) {
        VsatThptCalculationResult result = new VsatThptCalculationResult();
        result.setCalculated(true);

        MajorConfig major = majorByCode.get(normalize(form.getMajorCode()));
        if (major == null) {
            result.setMessage("Vui lòng chọn ngành xét tuyển.");
            return result;
        }

        boolean isVsat = "VSAT".equalsIgnoreCase(normalize(form.getMethodType()));
        Map<String, BigDecimal> subjectScores = normalizeSubjectScores(form, isVsat);

        BigDecimal englishConverted = round(
                clamp(nonNull(form.getEnglishConvertedScore()), BigDecimal.ZERO, THPT_MAX_SCORE));
        if (englishConverted.compareTo(subjectScores.get("TIENG_ANH")) > 0) {
            subjectScores.put("TIENG_ANH", englishConverted);
        }

        BigDecimal priorityScore = calculatePriorityScore(form.getPriorityObjectCode(), form.getPriorityRegionCode());
        BigDecimal bonusScore = round(clamp(nonNull(form.getBonusPoint()), BigDecimal.ZERO, BONUS_MAX_SCORE));
        String bonusSubjectCode = normalize(form.getBonusSubjectCode());

        List<CombinationScoreResult> combinationResults = new ArrayList<>();
        for (CombinationSpec combination : major.combinations()) {
            BigDecimal total = subjectScores.get(combination.subjectCode1())
                    .add(subjectScores.get(combination.subjectCode2()))
                    .add(subjectScores.get(combination.subjectCode3()))
                    .add(priorityScore);

            if (!"NONE".equalsIgnoreCase(bonusSubjectCode) && combination.containsSubject(bonusSubjectCode)) {
                total = total.add(bonusScore);
            }

            total = round(total);

            CombinationScoreResult scoreResult = new CombinationScoreResult();
            scoreResult.setCombinationCode(combination.code());
            scoreResult.setSubjectFormula(subjectLabelByCode.get(combination.subjectCode1())
                    + " + " + subjectLabelByCode.get(combination.subjectCode2())
                    + " + " + subjectLabelByCode.get(combination.subjectCode3()));
            scoreResult.setTotalScore(total);
            scoreResult.setThresholdScore(major.regularThreshold());
            scoreResult.setAdmissionScore(major.regularAdmission());
            scoreResult.setPassThreshold(total.compareTo(major.regularThreshold()) >= 0);
            scoreResult.setPassAdmission(
                    major.regularAdmission() != null && total.compareTo(major.regularAdmission()) >= 0);
            combinationResults.add(scoreResult);
        }

        result.setMethodType(isVsat ? "VSAT" : "THPT");
        result.setMethodLabel(isVsat ? "VSAT (quy đổi từ thang 150 về thang 10)" : "THPT (sử dụng thang 10)");
        result.setScaleNote(isVsat
                ? "Điểm thi VSAT được quy đổi về thang 10 trước khi tính điểm xét tuyển."
                : "Điểm thi THPT được sử dụng trực tiếp theo thang 10.");
        result.setMajorName(major.name());
        result.setPriorityScore(priorityScore);
        result.setBonusScore(bonusScore);
        result.setEnglishAppliedScore(subjectScores.get("TIENG_ANH"));
        result.setCombinationResults(combinationResults);
        result.setMessage("Đã hoàn tất tính điểm xét tuyển cho các tổ hợp của ngành đã chọn.");
        return result;
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
        BigDecimal bounded = clamp(nonNull(score), BigDecimal.ZERO, isVsat ? VSAT_MAX_SCORE : THPT_MAX_SCORE);
        if (isVsat) {
            return round(bounded.divide(VSAT_TO_10_DIVISOR, 6, RoundingMode.HALF_UP));
        }
        return round(bounded);
    }

    private BigDecimal calculatePriorityScore(String objectCode, String regionCode) {
        BigDecimal objectPoint = priorityObjectPoint.getOrDefault(normalize(objectCode), BigDecimal.ZERO);
        BigDecimal regionPoint = priorityRegionPoint.getOrDefault(normalize(regionCode), BigDecimal.ZERO);
        return round(objectPoint.add(regionPoint));
    }

    private boolean isAdmittedAspiration(XtNguyenVongXetTuyenEntity aspiration) {
        if (aspiration == null || aspiration.getNvKetQua() == null) {
            return false;
        }

        String value = normalizeAscii(aspiration.getNvKetQua());
        return value.contains("trung tuyen")
                || value.contains("dat")
                || value.equals("1")
                || value.equals("true");
    }

    private String buildFullName(XtThiSinhXetTuyen25Entity candidate) {
        String firstName = candidate.getHo() == null ? "" : candidate.getHo().trim();
        String lastName = candidate.getTen() == null ? "" : candidate.getTen().trim();
        return (firstName + " " + lastName).trim();
    }

    private String normalizeDisplayValue(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isBlank() ? fallback : normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCandidateCode(String value) {
        return normalize(value).toUpperCase(Locale.ROOT);
    }

    private String normalizeBirthDate(String value) {
        String digits = normalize(value).replaceAll("[^0-9]", "");
        if (digits.length() == 8) {
            String firstBlock = digits.substring(0, 4);
            if (Integer.parseInt(firstBlock) > 1900) {
                String yyyy = digits.substring(0, 4);
                String mm = digits.substring(4, 6);
                String dd = digits.substring(6, 8);
                return dd + mm + yyyy;
            }
        }
        return digits;
    }

    private String normalizeAscii(String value) {
        String normalized = Normalizer.normalize(normalize(value), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal round(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    private Map<String, MajorConfig> createMajors() {
        Map<String, MajorConfig> map = new LinkedHashMap<>();

        map.put("7480201", new MajorConfig(
                "7480201",
                "Công nghệ thông tin",
                "A00",
                new BigDecimal("20.50"),
                new BigDecimal("23.75"),
                new BigDecimal("18.00"),
                new BigDecimal("24.00"),
                List.of(
                        new CombinationSpec("A00", "TOAN", "VAT_LY", "HOA_HOC"),
                        new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"))));

        map.put("7310101", new MajorConfig(
                "7310101",
                "Kinh tế",
                "D01",
                new BigDecimal("18.50"),
                new BigDecimal("22.00"),
                new BigDecimal("17.00"),
                new BigDecimal("22.50"),
                List.of(
                        new CombinationSpec("A00", "TOAN", "VAT_LY", "HOA_HOC"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"),
                        new CombinationSpec("C00", "NGU_VAN", "LICH_SU", "DIA_LY"))));

        map.put("7510605", new MajorConfig(
                "7510605",
                "Logistics và quản lý chuỗi cung ứng",
                "A01",
                new BigDecimal("19.00"),
                null,
                new BigDecimal("17.50"),
                null,
                List.of(
                        new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"),
                        new CombinationSpec("C00", "NGU_VAN", "LICH_SU", "DIA_LY"))));

        return map;
    }

    private Map<String, BigDecimal> createPriorityObjectPointMap() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("NONE", BigDecimal.ZERO);
        map.put("UT1", new BigDecimal("2.00"));
        map.put("UT2", new BigDecimal("1.00"));
        map.put("UT3", new BigDecimal("0.50"));
        return map;
    }

    private Map<String, BigDecimal> createPriorityRegionPointMap() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("KV3", BigDecimal.ZERO);
        map.put("KV2", new BigDecimal("0.25"));
        map.put("KV2NT", new BigDecimal("0.50"));
        map.put("KV1", new BigDecimal("0.75"));
        return map;
    }

    private Map<String, String> createSubjectLabelMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("TOAN", "Toán");
        map.put("NGU_VAN", "Ngữ văn");
        map.put("TIENG_ANH", "Tiếng Anh");
        map.put("VAT_LY", "Vật lý");
        map.put("HOA_HOC", "Hóa học");
        map.put("SINH_HOC", "Sinh học");
        map.put("LICH_SU", "Lịch sử");
        map.put("DIA_LY", "Địa lý");
        map.put("GDCD", "GDCD");
        return map;
    }
}
