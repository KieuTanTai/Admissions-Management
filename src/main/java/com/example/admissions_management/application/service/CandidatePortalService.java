package com.example.admissions_management.application.service;

import com.example.admissions_management.application.service.candidate.AdmissionDecision;
import com.example.admissions_management.application.service.candidate.CandidateCredential;
import com.example.admissions_management.application.service.candidate.CandidateLookupResult;
import com.example.admissions_management.application.service.candidate.CombinationScoreResult;
import com.example.admissions_management.application.service.candidate.CombinationSpec;
import com.example.admissions_management.application.service.candidate.DgnlAspirationResult;
import com.example.admissions_management.application.service.candidate.DgnlCalculationResult;
import com.example.admissions_management.application.service.candidate.MajorConfig;
import com.example.admissions_management.application.service.candidate.OptionItem;
import com.example.admissions_management.application.service.candidate.VsatThptCalculationResult;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import com.example.admissions_management.presentation.web.model.DgnlCalculatorForm;
import com.example.admissions_management.presentation.web.model.VsatThptCalculatorForm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CandidatePortalService {

    private static final BigDecimal DGNL_MAX_SCORE = new BigDecimal("1200");
    private static final BigDecimal VSAT_MAX_SCORE = new BigDecimal("150");
    private static final BigDecimal THPT_MAX_SCORE = new BigDecimal("10");
    private static final BigDecimal BONUS_MAX_SCORE = new BigDecimal("3");
    private static final BigDecimal VSAT_TO_10_DIVISOR = new BigDecimal("15");

    private final Map<String, CandidateCredential> credentialByCccd;
    private final Map<String, AdmissionDecision> admissionByCccd;
    private final Map<String, MajorConfig> majorByCode;
    private final Map<String, BigDecimal> priorityObjectPoint;
    private final Map<String, BigDecimal> priorityRegionPoint;
    private final Map<String, String> subjectLabelByCode;

    public CandidatePortalService() {
        this.majorByCode = createMajors();
        this.credentialByCccd = createCredentials();
        this.admissionByCccd = createAdmissions();
        this.priorityObjectPoint = createPriorityObjectPointMap();
        this.priorityRegionPoint = createPriorityRegionPointMap();
        this.subjectLabelByCode = createSubjectLabelMap();
    }

    public List<OptionItem> getMajorOptions() {
        List<OptionItem> options = new ArrayList<>();
        for (MajorConfig major : majorByCode.values()) {
            options.add(new OptionItem(major.code(), major.code() + " - " + major.name()));
        }
        return options;
    }

    public List<OptionItem> getPriorityObjectOptions() {
        return List.of(
                new OptionItem("NONE", "Khong uu tien (0.00)"),
                new OptionItem("UT1", "Doi tuong uu tien 1 (+2.00)"),
                new OptionItem("UT2", "Doi tuong uu tien 2 (+1.00)"),
                new OptionItem("UT3", "Doi tuong uu tien 3 (+0.50)"));
    }

    public List<OptionItem> getPriorityRegionOptions() {
        return List.of(
                new OptionItem("KV3", "Khu vuc 3 (0.00)"),
                new OptionItem("KV2", "Khu vuc 2 (+0.25)"),
                new OptionItem("KV2NT", "Khu vuc 2-NT (+0.50)"),
                new OptionItem("KV1", "Khu vuc 1 (+0.75)"));
    }

    public List<OptionItem> getBonusSubjectOptions() {
        return List.of(
                new OptionItem("NONE", "Khong cong"),
                new OptionItem("TOAN", "Toan"),
                new OptionItem("VAT_LY", "Vat ly"),
                new OptionItem("HOA_HOC", "Hoa hoc"),
                new OptionItem("NGU_VAN", "Ngu van"),
                new OptionItem("TIENG_ANH", "Tieng Anh"),
                new OptionItem("SINH_HOC", "Sinh hoc"),
                new OptionItem("LICH_SU", "Lich su"),
                new OptionItem("DIA_LY", "Dia ly"),
                new OptionItem("GDCD", "GDCD"));
    }

    public CandidateLookupResult lookupResult(CandidateLookupForm form) {
        CandidateLookupResult result = new CandidateLookupResult();
        result.setSearched(true);

        String username = normalize(form.getUsername());
        String password = normalize(form.getPassword());

        if (!username.matches("\\d{8,20}") || !password.matches("\\d{8}")) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Thong tin dang nhap khong dung dinh dang.");
            return result;
        }

        CandidateCredential credential = credentialByCccd.get(username);
        if (credential == null || !credential.password().equals(password)) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Khong tim thay ket qua phu hop voi thong tin dang nhap.");
            return result;
        }

        result.setFound(true);
        result.setCccd(credential.cccd());
        result.setFullName(credential.fullName());

        AdmissionDecision decision = admissionByCccd.get(username);
        if (decision == null || !decision.admitted()) {
            result.setAdmitted(false);
            result.setMessage("Da tim thay thi sinh, ket qua: Khong trung tuyen.");
            return result;
        }

        MajorConfig major = majorByCode.get(decision.majorCode());
        result.setAdmitted(true);
        result.setMajorName(major != null ? major.name() : decision.majorCode());
        result.setScore(round(decision.score()));
        result.setCombination(decision.combinationCode());
        result.setMethod(decision.methodName());
        result.setMessage("Da tim thay ket qua: Trung tuyen.");
        return result;
    }

    public DgnlCalculationResult calculateDgnl(DgnlCalculatorForm form) {
        DgnlCalculationResult result = new DgnlCalculationResult();
        result.setCalculated(true);

        MajorConfig major = majorByCode.get(normalize(form.getMajorCode()));
        if (major == null) {
            result.setMessage("Vui long chon nganh xet tuyen.");
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
        result.setMessage("Da tinh diem DGNL va doi sanh voi diem nguong/diem trung tuyen.");
        return result;
    }

    public VsatThptCalculationResult calculateVsatThpt(VsatThptCalculatorForm form) {
        VsatThptCalculationResult result = new VsatThptCalculationResult();
        result.setCalculated(true);

        MajorConfig major = majorByCode.get(normalize(form.getMajorCode()));
        if (major == null) {
            result.setMessage("Vui long chon nganh xet tuyen.");
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
        result.setMethodLabel(isVsat ? "VSAT (quy doi tu thang 150 ve thang 10)" : "THPT (su dung thang 10)");
        result.setScaleNote(isVsat
                ? "Diem thi VSAT da duoc quy doi ve thang 10 truoc khi tinh diem xet tuyen."
                : "Diem thi THPT duoc su dung truc tiep theo thang 10.");
        result.setMajorName(major.name());
        result.setPriorityScore(priorityScore);
        result.setBonusScore(bonusScore);
        result.setEnglishAppliedScore(subjectScores.get("TIENG_ANH"));
        result.setCombinationResults(combinationResults);
        result.setMessage("Da tinh diem xet tuyen cac to hop mon cua nganh da chon.");
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

    private Map<String, CandidateCredential> createCredentials() {
        Map<String, CandidateCredential> map = new LinkedHashMap<>();
        map.put("079123456789", new CandidateCredential("079123456789", "Nguyen Van A", "15082007"));
        map.put("079999999999", new CandidateCredential("079999999999", "Tran Thi B", "01012007"));
        map.put("079111111111", new CandidateCredential("079111111111", "Le Van C", "24092007"));
        return map;
    }

    private Map<String, AdmissionDecision> createAdmissions() {
        Map<String, AdmissionDecision> map = new LinkedHashMap<>();
        map.put("079123456789", new AdmissionDecision(true, "7480201", new BigDecimal("24.15"), "A00", "DGNL"));
        map.put("079999999999", new AdmissionDecision(false, null, null, null, null));
        map.put("079111111111", new AdmissionDecision(true, "7510605", new BigDecimal("22.40"), "D01", "THPT"));
        return map;
    }

    private Map<String, MajorConfig> createMajors() {
        Map<String, MajorConfig> map = new LinkedHashMap<>();

        map.put("7480201", new MajorConfig(
                "7480201",
                "Cong nghe thong tin",
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
                "Kinh te",
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
                "Logistics va quan ly chuoi cung ung",
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
        map.put("TOAN", "Toan");
        map.put("NGU_VAN", "Ngu van");
        map.put("TIENG_ANH", "Tieng Anh");
        map.put("VAT_LY", "Vat ly");
        map.put("HOA_HOC", "Hoa hoc");
        map.put("SINH_HOC", "Sinh hoc");
        map.put("LICH_SU", "Lich su");
        map.put("DIA_LY", "Dia ly");
        map.put("GDCD", "GDCD");
        return map;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
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
}