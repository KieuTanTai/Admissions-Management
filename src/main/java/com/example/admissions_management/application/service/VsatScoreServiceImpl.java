package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.ScoreCalculationRequest;
import com.example.admissions_management.application.dto.response.CombinationResult;
import com.example.admissions_management.application.dto.response.ScoreResultResponse;
import com.example.admissions_management.application.service.candidate.CombinationSpec;
import com.example.admissions_management.application.service.candidate.MajorConfig;
import com.example.admissions_management.application.service.candidate.OptionItem;
import com.example.admissions_management.domain.model.Combination;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.application.service.BangQuyDoiService;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class VsatScoreServiceImpl implements VsatScoreService {

    private static final String METHOD_VSAT = "VSAT";
    private static final String METHOD_THPT = "THPT";
    private static final String DEFAULT_WARNING = "Lỗi, điểm nhập vào không nằm trong phân vị nào = 0";
    private static final double MAX_THPT_SCORE = 10.0d;
    private static final double MAX_VSAT_SCORE = 150.0d;
    private static final double MAX_PRIORITY_SCORE = 3.0d;

    private final Map<String, List<ScoreInterval>> intervalsBySubject;
    private final Map<String, Double> priorityObjectPoint;
    private final Map<String, Double> priorityRegionPoint;
    private final Map<String, String> subjectLabelByCode;
    private final MajorRepository majorRepository;
    private final ICombinationRepository combinationRepository;
    private final BangQuyDoiService bangQuyDoiService;

    public VsatScoreServiceImpl(MajorRepository majorRepository, ICombinationRepository combinationRepository, BangQuyDoiService bangQuyDoiService) {
        this.majorRepository = majorRepository;
        this.combinationRepository = combinationRepository;
        this.bangQuyDoiService = bangQuyDoiService;
        this.intervalsBySubject = createIntervals();
        this.priorityObjectPoint = createPriorityObjectPoints();
        this.priorityRegionPoint = createPriorityRegionPoints();
        this.subjectLabelByCode = createSubjectLabels();
    }

    @Override
    public List<String> getConvertibleSubjectCodes() {
        LinkedHashSet<String> codes = new LinkedHashSet<>(intervalsBySubject.keySet());
        try {
            var all = bangQuyDoiService.getAll();
            if (all != null) {
                for (var b : all) {
                    if (b == null) {
                        continue;
                    }
                    if (!METHOD_VSAT.equalsIgnoreCase(normalize(b.getPhuongThuc()))) {
                        continue;
                    }
                    String mon = b.getMon();
                    if (mon != null && !mon.isBlank()) {
                        codes.add(normalizeSubjectCode(mon));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(codes);
    }

    @Override
    public List<OptionItem> getMajorOptions() {
        Map<String, MajorConfig> majorByCode = loadMajorConfigs();
        List<OptionItem> options = new ArrayList<>();
        for (MajorConfig major : majorByCode.values()) {
            options.add(new OptionItem(major.code(), major.code() + " - " + major.name()));
        }
        return options;
    }

    @Override
    public List<OptionItem> getPriorityObjectOptions() {
        return List.of(
                new OptionItem("NONE", "Không ưu tiên (0.00)"),
                new OptionItem("DT1", "Đối tượng ưu tiên 1 (+2.00)"),
                new OptionItem("DT2", "Đối tượng ưu tiên 2 (+1.00)"),
                new OptionItem("DT3", "Đối tượng ưu tiên 3 (+0.50)"),
                new OptionItem("DT4", "Đối tượng ưu tiên 4 (+0.25)"),
                new OptionItem("DT5", "Đối tượng ưu tiên 5 (+0.25)"),
                new OptionItem("DT6", "Đối tượng ưu tiên 6 (+0.25)"),
                new OptionItem("DT7", "Đối tượng ưu tiên 7 (+0.25)"));
    }

    @Override
    public List<OptionItem> getPriorityRegionOptions() {
        return List.of(
                new OptionItem("KV3", "Khu vực 3 (0.00)"),
                new OptionItem("KV2", "Khu vực 2 (+0.25)"),
                new OptionItem("KV2NT", "Khu vực 2-NT (+0.50)"),
                new OptionItem("KV1", "Khu vực 1 (+0.75)"));
    }

    @Override
    public List<OptionItem> getBonusSubjectOptions() {
        List<OptionItem> options = new ArrayList<>();
        options.add(new OptionItem("NONE", "Không cộng điểm"));
        for (String code : getConvertibleSubjectCodes()) {
            options.add(new OptionItem(code, subjectLabelByCode.getOrDefault(code, code)));
        }
        return options;
    }

    @Override
    public ScoreResultResponse calculate(ScoreCalculationRequest request) {
        ScoreResultResponse response = new ScoreResultResponse();
        response.setCalculated(true);

        Map<String, MajorConfig> majorByCode = loadMajorConfigs();
        MajorConfig major = majorByCode.get(normalize(request.getMaNganh()));
        if (major == null) {
            response.setCalculated(false);
            response.setMessage("Vui lòng chọn ngành xét tuyển hợp lệ.");
            return response;
        }

        boolean isVsat = METHOD_VSAT.equalsIgnoreCase(normalize(request.getLoaiDiem()));
        Map<String, SubjectConversion> conversions = convertAllSubjects(request, isVsat, response.getWarnings());

        double priorityScore = calculatePriorityScore(request.getDoiTuong(), request.getKhuVuc());
        double bonusScore = clamp(defaultDouble(request.getDiemCong()), 0.0d, MAX_PRIORITY_SCORE);
        String bonusSubject = normalize(request.getMonCongDiem());

        List<CombinationResult> combinationResults = new ArrayList<>();
        for (CombinationSpec combination : major.combinations()) {
            SubjectConversion subject1 = resolveConversion(conversions, combination.subjectCode1(), response.getWarnings());
            SubjectConversion subject2 = resolveConversion(conversions, combination.subjectCode2(), response.getWarnings());
            SubjectConversion subject3 = resolveConversion(conversions, combination.subjectCode3(), response.getWarnings());

            if (!subject1.provided() || !subject2.provided() || !subject3.provided()) {
                continue;
            }

            double tongDiem3Mon = round2(subject1.convertedScore() + subject2.convertedScore() + subject3.convertedScore());
            double tongHeSo = weightOf(combination);
            double tongDiemCoHeSo = round2(
                    subject1.convertedScore() * combination.weight1()
                            + subject2.convertedScore() * combination.weight2()
                            + subject3.convertedScore() * combination.weight3());
            double diemToHopXetTuyen = tongHeSo > 0.0d ? round2(tongDiemCoHeSo / tongHeSo * 3.0d) : tongDiem3Mon;
            double doLechToHopGoc = combination.doLech() == null ? 0.0d : combination.doLech().doubleValue();
            double diemCongMon = appliedBonusForCombination(bonusSubject, bonusScore, combination);
            double diemXetNguong = round2(tongDiem3Mon + priorityScore);
            double diemXetTuyenCuoiCung = round2(diemToHopXetTuyen + priorityScore + diemCongMon - doLechToHopGoc);
            double diemNguongSan = major.regularThreshold() == null ? 0.0d : major.regularThreshold().doubleValue();

            CombinationResult result = new CombinationResult();
            result.setMaToHop(combination.code());
            result.setTenToHop(combination.code() + " - "
                    + subjectLabelByCode.getOrDefault(combination.subjectCode1(), combination.subjectCode1())
                    + "/" + subjectLabelByCode.getOrDefault(combination.subjectCode2(), combination.subjectCode2())
                    + "/" + subjectLabelByCode.getOrDefault(combination.subjectCode3(), combination.subjectCode3()));
            result.setChuoiCongThucChiTiet(buildFormulaText(subject1, subject2, subject3));
            result.setDiemQuyDoiToHop(tongDiem3Mon);
            result.setDiemXetNguong(diemXetNguong);
            result.setDiemToHopXetTuyen(diemToHopXetTuyen);
            result.setDoLechToHopGoc(doLechToHopGoc);
            result.setDiemXetTuyenCuoiCung(diemXetTuyenCuoiCung);
            result.setDiemUuTien(priorityScore);
            result.setDiemCongMon(diemCongMon);
            result.setDatNguong(Double.compare(diemXetNguong, diemNguongSan) >= 0);
            result.setDatDiemXetTuyen(major.regularAdmission() != null && diemXetTuyenCuoiCung >= major.regularAdmission().doubleValue());
            combinationResults.add(result);
        }

        response.setTenNganh(major.name());
        response.setLoaiDiemLabel(isVsat ? "Điểm thi V-SAT (thang 150, quy đổi theo nội suy)" : "Điểm thi THPT (thang 10)");
        response.setMonCongDiemLabel("NONE".equalsIgnoreCase(normalize(request.getMonCongDiem()))
            ? "Không cộng điểm"
            : subjectLabelByCode.getOrDefault(normalize(request.getMonCongDiem()), "Không chọn"));
        response.setDiemUuTien(priorityScore);
        response.setDiemCongMon(bonusScore);
        response.setTongDiemCong(round2(priorityScore + bonusScore));
        response.setCombinationResults(combinationResults);
        if (combinationResults.isEmpty()) {
            response.setMessage("Không có tổ hợp nào đủ 3 môn để tính từ dữ liệu đã nhập.");
        } else {
            response.setMessage(response.getWarnings().isEmpty()
                    ? "Đã tính điểm xét tuyển cho các tổ hợp môn của ngành đã chọn."
                    : "Đã tính điểm xét tuyển, một số môn có cảnh báo quy đổi.");
        }
        return response;
    }

    private Map<String, SubjectConversion> convertAllSubjects(ScoreCalculationRequest request, boolean isVsat, List<String> warnings) {
        Map<String, SubjectConversion> map = new LinkedHashMap<>();
        
        // Handle English certificate if provided (IELTS/TOEFL/TOEIC/PTE/...)
        Double effectiveDiemAnh = request.getDiemAnh();
        if (request.getLoaiChungChiAnh() != null && !request.getLoaiChungChiAnh().isBlank()) {
            
            // Special handling for TOEIC 4 skills
            if ("TOEIC".equalsIgnoreCase(request.getLoaiChungChiAnh())) {
                Double toeicScore = convertToeic4Skills(
                        request.getDiemToeicNghe(),
                        request.getDiemToeicDoc(),
                        request.getDiemToeicNoi(),
                        request.getDiemToeicViet(),
                        warnings);
                if (toeicScore != null && toeicScore > 0) {
                    effectiveDiemAnh = toeicScore;
                }
            } else if ("APTIS_ESOL_GENERAL".equalsIgnoreCase(request.getLoaiChungChiAnh()) 
                    || "APTIS_ESOL_ADVANCED".equalsIgnoreCase(request.getLoaiChungChiAnh())) {
                // Aptis ESOL: diemChungChiAnh contains the level (B1, B2, C1)
                Double aptisScore = convertAptisEsol(request.getLoaiChungChiAnh(), request.getDiemChungChiAnh(), warnings);
                if (aptisScore != null && aptisScore > 0) {
                    effectiveDiemAnh = aptisScore;
                }
            } else if ("VSTEP".equalsIgnoreCase(request.getLoaiChungChiAnh())) {
                // VSTEP: diemChungChiAnh contains the level (3, 4, 5)
                Double vstepScore = convertVstep(request.getDiemChungChiAnh(), warnings);
                if (vstepScore != null && vstepScore > 0) {
                    effectiveDiemAnh = vstepScore;
                }
            } else if (request.getDiemChungChiAnh() != null && request.getDiemChungChiAnh() > 0) {
                // Standard single-score certificates (IELTS, TOEFL, PTE, Linguaskill)
                Double certificateScore = convertEnglishCertificate(
                        request.getLoaiChungChiAnh(), 
                        request.getDiemChungChiAnh(),
                        request.getKyNangToeic(),
                        warnings);
                if (certificateScore != null && certificateScore > 0) {
                    effectiveDiemAnh = certificateScore;
                }
            }
        }
        
        map.put("TOAN", convertSubject("TOAN", request.getDiemToan(), isVsat, warnings));
        map.put("NGU_VAN", convertSubject("NGU_VAN", request.getDiemVan(), isVsat, warnings));
        map.put("TIENG_ANH", convertSubject("TIENG_ANH", effectiveDiemAnh, isVsat, warnings));
        map.put("VAT_LY", convertSubject("VAT_LY", request.getDiemLy(), isVsat, warnings));
        map.put("HOA_HOC", convertSubject("HOA_HOC", request.getDiemHoa(), isVsat, warnings));
        map.put("SINH_HOC", convertSubject("SINH_HOC", request.getDiemSinh(), isVsat, warnings));
        map.put("LICH_SU", convertSubject("LICH_SU", request.getDiemSu(), isVsat, warnings));
        map.put("DIA_LY", convertSubject("DIA_LY", request.getDiemDia(), isVsat, warnings));
        map.put("KTPL", convertSubject("KTPL", request.getDiemKtpl(), isVsat, warnings));
        map.put("TI", convertSubject("TI", request.getDiemTi(), isVsat, warnings));
        map.put("CNCN", convertSubject("CNCN", request.getDiemCncn(), isVsat, warnings));
        map.put("CNNN", convertSubject("CNNN", request.getDiemCnnn(), isVsat, warnings));
        return map;
    }

    private SubjectConversion convertSubject(String subjectCode, Double rawScore, boolean isVsat, List<String> warnings) {
        boolean provided = rawScore != null && Double.compare(rawScore.doubleValue(), 0.0d) != 0;
        double score = clamp(defaultDouble(rawScore), 0.0d, isVsat ? MAX_VSAT_SCORE : MAX_THPT_SCORE);
        String label = subjectLabelByCode.getOrDefault(subjectCode, subjectCode);

        if (!isVsat) {
            double converted = round2(score);
            return new SubjectConversion(subjectCode, provided, score, converted,
                    label + ": " + format(score) + " (THPT trực tiếp) = " + format(converted));
        }

        if (score <= 0.0d) {
            if (provided) {
                warnings.add(label + ": " + DEFAULT_WARNING);
                return new SubjectConversion(subjectCode, true, score, 0.0d, label + ": " + DEFAULT_WARNING);
            }
            return new SubjectConversion(subjectCode, false, score, 0.0d, label + ": không có dữ liệu (được coi là thiếu)");
        }

        try {
            Map<String, String> ketqua = bangQuyDoiService.quyDoiDiemKhaoThi(METHOD_VSAT, subjectCode, BigDecimal.valueOf(score));
            if (ketqua != null && ketqua.containsKey("diemQuyDoi")) {
                double converted = Double.parseDouble(ketqua.get("diemQuyDoi"));
                String formula = label + ": " + ketqua.getOrDefault("congThuc", "") + " = " + ketqua.get("diemQuyDoi");
                return new SubjectConversion(subjectCode, provided, score, converted, formula);
            }
        } catch (Exception e) {
            // fall through to interval-based interpolation
        }

        ScoreInterval interval = findInterval(subjectCode, score);
        if (interval == null) {
            warnings.add(label + ": " + DEFAULT_WARNING);
            return new SubjectConversion(subjectCode, provided, score, 0.0d, label + ": " + DEFAULT_WARNING);
        }

        double converted = calculateInterpolatedScore(score, interval);
        String formula = label + ": " + format(interval.yLowerExclusive()) + " + ("
                + format(score) + " - " + format(interval.xLowerExclusive()) + ")/("
                + format(interval.xUpperInclusive()) + " - " + format(interval.xLowerExclusive()) + ")*("
                + format(interval.yUpperInclusive()) + " - " + format(interval.yLowerExclusive()) + ") = " + format(converted);
        return new SubjectConversion(subjectCode, provided, score, converted, formula);
    }

    public ScoreInterval findInterval(String monHoc, double score) {
        List<ScoreInterval> intervals = intervalsBySubject.get(normalize(monHoc));
        if (intervals == null || intervals.isEmpty()) {
            return null;
        }
        for (ScoreInterval interval : intervals) {
            if (interval.contains(score)) {
                return interval;
            }
        }
        return null;
    }

    /**
     * Convert English certificate (IELTS/TOEFL/TOEIC/PTE/Linguaskill/Aptis/VSTEP) score to 10-point scale.
     * Returns the converted score if successful, or null if conversion fails.
     * 
     * For TOEIC, needs kyNangToeic (NGHE, DOC, NOI, VIET) to query correct conversion table.
     */
    private Double convertEnglishCertificate(String certificateType, Double certificateScore, String kyNangToeic, List<String> warnings) {
        if (certificateType == null || certificateType.isBlank() || certificateScore == null || certificateScore <= 0) {
            return null;
        }
        
        try {
            // For TOEIC with 4 skills, append skill to certificate type for correct lookup
            String phuongThucQuery = certificateType;
            if ("TOEIC".equalsIgnoreCase(certificateType) && kyNangToeic != null && !kyNangToeic.isBlank()) {
                phuongThucQuery = "TOEIC_" + kyNangToeic.toUpperCase();
            }
            
            // Query database for conversion rule matching the score range
            Double converted = bangQuyDoiService.quyDoiDiemNgoaiNgu(phuongThucQuery, "TIENG_ANH", 
                    BigDecimal.valueOf(certificateScore));
            
            if (converted != null && converted > 0) {
                String certificateLabel = formatCertificateLabel(certificateType, kyNangToeic);
                warnings.add("Tiếng Anh: Sử dụng quy đổi từ " + certificateLabel + " (" 
                        + format(certificateScore) + ") = " + format(converted));
                return converted;
            } else {
                // If database lookup returns 0, score is out of range or not found
                String certificateLabel = formatCertificateLabel(certificateType, kyNangToeic);
                warnings.add("Tiếng Anh: Điểm " + certificateLabel + " (" + format(certificateScore) 
                        + ") không nằm trong bảng quy đổi được hỗ trợ. Sử dụng điểm thi thay thế.");
                return null;
            }
        } catch (Exception e) {
            String certificateLabel = formatCertificateLabel(certificateType, kyNangToeic);
            warnings.add("Tiếng Anh: Lỗi khi quy đổi " + certificateLabel + ": " + e.getMessage());
            return null;
        }
    }
    
    private String formatCertificateLabel(String certificateType, String kyNangToeic) {
        if (certificateType == null) return "chứng chỉ";
        
        return switch (certificateType) {
            case "IELTS" -> "IELTS";
            case "TOEFL_ITP" -> "TOEFL ITP";
            case "TOEFL_IBT" -> "TOEFL iBT";
            case "TOEIC" -> {
                String skill = switch (kyNangToeic) {
                    case "NGHE" -> " (Nghe)";
                    case "DOC" -> " (Đọc)";
                    case "NOI" -> " (Nói)";
                    case "VIET" -> " (Viết)";
                    default -> "";
                };
                yield "TOEIC" + skill;
            }
            case "PTE_ACADEMIC" -> "PTE Academic";
            case "LINGUASKILL" -> "Linguaskill";
            case "APTIS_ESOL_GENERAL" -> "Aptis ESOL (General)";
            case "APTIS_ESOL_ADVANCED" -> "Aptis ESOL (Advanced)";
            case "VSTEP" -> "VSTEP (3 bậc)";
            default -> certificateType;
        };
    }

    /**
     * Convert TOEIC 4 skills to 10-point scale.
     * Only converts if ALL 4 skills meet their respective thresholds.
     * Thresholds:
     * - Level 1: Listening 275-399, Reading 275-399, Speaking 120-159, Writing 120-159
     * - Level 2: Listening 400-489, Reading 385-454, Speaking 160-179, Writing 150-179
     * - Level 3: Listening >=490, Reading >=455, Speaking >=180, Writing >=180
     */
    private Double convertToeic4Skills(Double nghe, Double doc, Double noi, Double viet, List<String> warnings) {
        // Check if any skill is provided
        boolean hasNghe = nghe != null && nghe > 0;
        boolean hasDoc = doc != null && doc > 0;
        boolean hasNoi = noi != null && noi > 0;
        boolean hasViet = viet != null && viet > 0;
        
        if (!hasNghe && !hasDoc && !hasNoi && !hasViet) {
            return null;
        }
        
        // Normalize null/0 values
        double listeningScore = hasNghe ? nghe : 0;
        double readingScore = hasDoc ? doc : 0;
        double speakingScore = hasNoi ? noi : 0;
        double writingScore = hasViet ? viet : 0;
        
        // Determine level based on ALL 4 skills
        int listeningLevel = getToeicSkillLevel(listeningScore, "NGHE");
        int readingLevel = getToeicSkillLevel(readingScore, "DOC");
        int speakingLevel = getToeicSkillLevel(speakingScore, "NOI");
        int writingLevel = getToeicSkillLevel(writingScore, "VIET");
        
        // Only convert if ALL 4 skills meet at least level 1 threshold
        if (listeningLevel == 0 || readingLevel == 0 || speakingLevel == 0 || writingLevel == 0) {
            // At least one skill doesn't meet minimum threshold
            String failedSkills = "";
            if (listeningLevel == 0) failedSkills += "Nghe ";
            if (readingLevel == 0) failedSkills += "Đọc ";
            if (speakingLevel == 0) failedSkills += "Nói ";
            if (writingLevel == 0) failedSkills += "Viết ";
            warnings.add("TOEIC: Các kỹ năng sau không đạt mức tối thiểu: " + failedSkills.trim());
            return null;
        }
        
        // Determine final level: take minimum level across all 4 skills
        int finalLevel = Math.min(Math.min(listeningLevel, readingLevel), 
                                  Math.min(speakingLevel, writingLevel));
        
        try {
            // Get conversion score from database for each skill at the final level
            Double listeningConverted = bangQuyDoiService.quyDoiDiemNgoaiNgu("TOEIC_NGHE", "TIENG_ANH", 
                    BigDecimal.valueOf(listeningScore));
            Double readingConverted = bangQuyDoiService.quyDoiDiemNgoaiNgu("TOEIC_DOC", "TIENG_ANH", 
                    BigDecimal.valueOf(readingScore));
            Double speakingConverted = bangQuyDoiService.quyDoiDiemNgoaiNgu("TOEIC_NOI", "TIENG_ANH", 
                    BigDecimal.valueOf(speakingScore));
            Double writingConverted = bangQuyDoiService.quyDoiDiemNgoaiNgu("TOEIC_VIET", "TIENG_ANH", 
                    BigDecimal.valueOf(writingScore));
            
            // Average all converted scores
            double totalConverted = (listeningConverted != null ? listeningConverted : 0) +
                                  (readingConverted != null ? readingConverted : 0) +
                                  (speakingConverted != null ? speakingConverted : 0) +
                                  (writingConverted != null ? writingConverted : 0);
            double avgConverted = round2(totalConverted / 4);
            
            String details = String.format("TOEIC: Nghe=%d, Đọc=%d, Nói=%d, Viết=%d (Mức %d) -> %.1f",
                    nghe != null ? nghe.intValue() : 0,
                    doc != null ? doc.intValue() : 0,
                    noi != null ? noi.intValue() : 0,
                    viet != null ? viet.intValue() : 0,
                    finalLevel, avgConverted);
            warnings.add("Tiếng Anh: " + details);
            
            return avgConverted;
        } catch (Exception e) {
            warnings.add("Tiếng Anh: Lỗi khi quy đổi TOEIC 4 kỹ năng: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get TOEIC skill level (0-3) based on score ranges.
     * Level 0: below minimum (no conversion)
     * Level 1: 275-399 (Listening/Reading) or 120-159 (Speaking/Writing)
     * Level 2: 400-489 (Listening) / 385-454 (Reading) or 160-179 (Speaking/Writing)
     * Level 3: >=490 (Listening) / >=455 (Reading) or >=180 (Speaking/Writing)
     */
    private int getToeicSkillLevel(double score, String skillCode) {
        if (score <= 0) return 0;
        
        if ("NGHE".equals(skillCode) || "DOC".equals(skillCode)) {
            // Listening and Reading: max 495
            if (score < 275) return 0;
            if (score < 400) return 1;
            if (score < 490) return 2;
            if (score >= 490) return 3;
        } else {
            // Speaking and Writing: max 200
            if (score < 120) return 0;
            if (score < 160) return 1;
            if (score < 180) return 2;
            if (score >= 180) return 3;
        }
        return 0;
    }
    
    /**
     * Convert Aptis ESOL level to 10-point scale.
     * B1 -> Level 1, B2 -> Level 2, C1 -> Level 3
     */
    private Double convertAptisEsol(String certificateType, Double levelValue, List<String> warnings) {
        if (levelValue == null || levelValue <= 0) {
            warnings.add("Tiếng Anh: Vui lòng chọn mức bằng Aptis ESOL");
            return null;
        }
        
        try {
            // levelValue is a numeric representation (converted from B1, B2, C1 in frontend)
            // We need to convert it back or use it directly with the database
            String level = "";
            if (levelValue == 1) level = "B1";
            else if (levelValue == 2) level = "B2";
            else if (levelValue == 3) level = "C1";
            else {
                warnings.add("Tiếng Anh: Mức bằng Aptis ESOL không hợp lệ");
                return null;
            }
            
            Double converted = bangQuyDoiService.quyDoiDiemNgoaiNgu(certificateType, "TIENG_ANH", 
                    BigDecimal.valueOf(levelValue));
            
            if (converted != null && converted > 0) {
                String certLabel = "APTIS_ESOL_GENERAL".equals(certificateType) ? 
                        "Aptis ESOL (General)" : "Aptis ESOL (Advanced)";
                warnings.add("Tiếng Anh: Sử dụng quy đổi từ " + certLabel + " (" + level + ") = " + format(converted));
                return converted;
            } else {
                warnings.add("Tiếng Anh: Aptis ESOL mức " + level + " không tìm thấy bảng quy đổi");
                return null;
            }
        } catch (Exception e) {
            warnings.add("Tiếng Anh: Lỗi khi quy đổi Aptis ESOL: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert VSTEP level to 10-point scale.
     * Bậc 3 -> Level 1, Bậc 4 -> Level 2, Bậc 5 -> Level 3
     */
    private Double convertVstep(Double levelValue, List<String> warnings) {
        if (levelValue == null || levelValue < 3 || levelValue > 5) {
            warnings.add("Tiếng Anh: Vui lòng chọn mức bằng VSTEP (3, 4, hoặc 5)");
            return null;
        }
        
        try {
            Double converted = bangQuyDoiService.quyDoiDiemNgoaiNgu("VSTEP", "TIENG_ANH", 
                    BigDecimal.valueOf(levelValue));
            
            if (converted != null && converted > 0) {
                String level = "Bậc " + levelValue.intValue();
                warnings.add("Tiếng Anh: Sử dụng quy đổi từ VSTEP (" + level + ") = " + format(converted));
                return converted;
            } else {
                warnings.add("Tiếng Anh: VSTEP mức " + levelValue.intValue() + " không tìm thấy bảng quy đổi");
                return null;
            }
        } catch (Exception e) {
            warnings.add("Tiếng Anh: Lỗi khi quy đổi VSTEP: " + e.getMessage());
            return null;
        }
    }

    private double calculateInterpolatedScore(double x, ScoreInterval interval) {
        BigDecimal a = BigDecimal.valueOf(interval.xLowerExclusive());
        BigDecimal b = BigDecimal.valueOf(interval.xUpperInclusive());
        BigDecimal c = BigDecimal.valueOf(interval.yLowerExclusive());
        BigDecimal d = BigDecimal.valueOf(interval.yUpperInclusive());
        BigDecimal value = c.add(BigDecimal.valueOf(x).subtract(a)
                .divide(b.subtract(a), 12, RoundingMode.HALF_UP)
                .multiply(d.subtract(c)));
        return round2(value.doubleValue());
    }

    private String buildFormulaText(SubjectConversion subject1, SubjectConversion subject2, SubjectConversion subject3) {
        return subject1.formulaText() + " | " + subject2.formulaText() + " | " + subject3.formulaText();
    }

    private double appliedBonusForCombination(String bonusSubject, double bonusScore, CombinationSpec combination) {
        if (bonusScore <= 0.0d || "NONE".equalsIgnoreCase(bonusSubject)) {
            return 0.0d;
        }
        return combination.containsSubject(bonusSubject) ? bonusScore : 0.0d;
    }

    private double calculatePriorityScore(String doiTuong, String khuVuc) {
        double objectPoint = priorityObjectPoint.getOrDefault(normalize(doiTuong), 0.0d);
        double regionPoint = priorityRegionPoint.getOrDefault(normalize(khuVuc), 0.0d);
        return round2(objectPoint + regionPoint);
    }

    private double weightOf(CombinationSpec combination) {
        return combination.weight1() + combination.weight2() + combination.weight3();
    }

    private Map<String, MajorConfig> createMajors() {
        Map<String, MajorConfig> map = new LinkedHashMap<>();
        map.put("7480201", new MajorConfig(
                "7480201",
                "Công nghệ thông tin",
                "A01",
                BigDecimal.valueOf(18.0),
                BigDecimal.valueOf(24.0),
                BigDecimal.valueOf(20.5),
                BigDecimal.valueOf(23.75),
                List.of(
                        new CombinationSpec("A00", "TOAN", "VAT_LY", "HOA_HOC"),
                        new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"))));

        map.put("7340101", new MajorConfig(
                "7340101",
                "Quản trị kinh doanh",
                "D01",
                BigDecimal.valueOf(17.0),
                BigDecimal.valueOf(22.0),
                BigDecimal.valueOf(18.5),
                BigDecimal.valueOf(22.5),
                List.of(
                        new CombinationSpec("A00", "TOAN", "VAT_LY", "HOA_HOC"),
                        new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"))));

        map.put("7720201", new MajorConfig(
                "7720201",
                "Dược học",
                "B00",
                BigDecimal.valueOf(22.0),
                BigDecimal.valueOf(24.5),
                BigDecimal.valueOf(21.0),
                BigDecimal.valueOf(23.5),
                List.of(
                        new CombinationSpec("B00", "TOAN", "HOA_HOC", "SINH_HOC"),
                        new CombinationSpec("A00", "TOAN", "VAT_LY", "HOA_HOC"),
                        new CombinationSpec("D07", "TOAN", "HOA_HOC", "TIENG_ANH"))));

        map.put("7220201", new MajorConfig(
                "7220201",
                "Ngôn ngữ Anh",
                "D01",
                BigDecimal.valueOf(19.0),
                BigDecimal.valueOf(21.0),
                BigDecimal.valueOf(18.0),
                BigDecimal.valueOf(20.5),
                List.of(
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"),
                        new CombinationSpec("D14", "NGU_VAN", "LICH_SU", "TIENG_ANH"),
                        new CombinationSpec("D15", "NGU_VAN", "DIA_LY", "TIENG_ANH"))));

        map.put("7380101", new MajorConfig(
                "7380101",
                "Luật",
                "C00",
                BigDecimal.valueOf(18.5),
                BigDecimal.valueOf(21.0),
                BigDecimal.valueOf(17.5),
                BigDecimal.valueOf(20.0),
                List.of(
                        new CombinationSpec("C00", "NGU_VAN", "LICH_SU", "DIA_LY"),
                        new CombinationSpec("D01", "TOAN", "NGU_VAN", "TIENG_ANH"),
                        new CombinationSpec("A01", "TOAN", "VAT_LY", "TIENG_ANH"))));

        return map;
    }

    private Map<String, List<ScoreInterval>> createIntervals() {
        Map<String, List<ScoreInterval>> map = new LinkedHashMap<>();
        map.put("TOAN", List.of(
                new ScoreInterval(132.0, 150.0, 8.5, 10.0),
                new ScoreInterval(128.5, 132.0, 8.1, 8.5),
                new ScoreInterval(122.5, 128.5, 7.75, 8.1),
                new ScoreInterval(114.5, 122.5, 7.0, 7.75),
                new ScoreInterval(108.0, 114.5, 6.6, 7.0),
                new ScoreInterval(102.5, 108.0, 6.25, 6.6),
                new ScoreInterval(97.0, 102.5, 6.0, 6.25),
                new ScoreInterval(91.0, 97.0, 5.6, 6.0),
                new ScoreInterval(85.0, 91.0, 5.25, 5.6),
                new ScoreInterval(77.0, 85.0, 5.0, 5.25),
                new ScoreInterval(68.0, 77.0, 4.5, 5.0),
                new ScoreInterval(6.0, 68.0, 1.5, 4.5)));

        map.put("NGU_VAN", List.of(
                new ScoreInterval(124.0, 150.0, 9.0, 10.0),
                new ScoreInterval(119.5, 124.0, 8.75, 9.0),
                new ScoreInterval(112.0, 119.5, 8.25, 8.75),
                new ScoreInterval(104.5, 112.0, 7.75, 8.25),
                new ScoreInterval(97.0, 104.5, 7.25, 7.75),
                new ScoreInterval(90.0, 97.0, 6.75, 7.25),
                new ScoreInterval(82.0, 90.0, 6.25, 6.75),
                new ScoreInterval(74.0, 82.0, 5.75, 6.25),
                new ScoreInterval(66.0, 74.0, 5.25, 5.75),
                new ScoreInterval(58.0, 66.0, 4.75, 5.25),
                new ScoreInterval(50.0, 58.0, 4.0, 4.75),
                new ScoreInterval(6.0, 50.0, 1.5, 4.0)));

        map.put("TIENG_ANH", genericIntervals());
        map.put("VAT_LY", genericIntervals());
        map.put("HOA_HOC", genericIntervals());
        map.put("SINH_HOC", genericIntervals());
        map.put("LICH_SU", genericIntervals());
        map.put("DIA_LY", genericIntervals());
        map.put("KTPL", genericIntervals());
        map.put("TI", genericIntervals());
        map.put("CNCN", genericIntervals());
        map.put("CNNN", genericIntervals());
        return map;
    }

    private List<ScoreInterval> genericIntervals() {
        return List.of(
                new ScoreInterval(132.0, 150.0, 8.5, 10.0),
                new ScoreInterval(128.5, 132.0, 8.1, 8.5),
                new ScoreInterval(122.5, 128.5, 7.75, 8.1),
                new ScoreInterval(114.5, 122.5, 7.0, 7.75),
                new ScoreInterval(108.0, 114.5, 6.6, 7.0),
                new ScoreInterval(102.5, 108.0, 6.25, 6.6),
                new ScoreInterval(97.0, 102.5, 6.0, 6.25),
                new ScoreInterval(91.0, 97.0, 5.6, 6.0),
                new ScoreInterval(85.0, 91.0, 5.25, 5.6),
                new ScoreInterval(77.0, 85.0, 5.0, 5.25),
                new ScoreInterval(68.0, 77.0, 4.5, 5.0),
                new ScoreInterval(6.0, 68.0, 1.5, 4.5));
    }

    private Map<String, Double> createPriorityObjectPoints() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("NONE", 0.0d);
        map.put("DT1", 2.0d);
        map.put("DT2", 1.0d);
        map.put("DT3", 0.5d);
        map.put("DT4", 0.25d);
        map.put("DT5", 0.25d);
        map.put("DT6", 0.25d);
        map.put("DT7", 0.25d);
        return map;
    }

    private Map<String, Double> createPriorityRegionPoints() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("KV3", 0.0d);
        map.put("KV2", 0.25d);
        map.put("KV2NT", 0.5d);
        map.put("KV1", 0.75d);
        return map;
    }

    private Map<String, String> createSubjectLabels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("TOAN", "Toan");
        map.put("NGU_VAN", "Ngu van");
        map.put("TIENG_ANH", "Tieng Anh");
        map.put("VAT_LY", "Vat ly");
        map.put("HOA_HOC", "Hoa hoc");
        map.put("SINH_HOC", "Sinh hoc");
        map.put("LICH_SU", "Lich su");
        map.put("DIA_LY", "Dia ly");
        map.put("TI", "Tin hoc");
        map.put("CNCN", "Cong nghe cong nghiep");
        map.put("CNNN", "Cong nghe nong nghiep");
        map.put("KTPL", "Kinh te phap luat");
        return map;
    }
    private Map<String, MajorConfig> loadMajorConfigs() {
        Map<String, MajorConfig> databaseMajors = loadMajorConfigsFromDatabase();
        if (!databaseMajors.isEmpty()) {
            return databaseMajors;
        }
        return createMajors();
    }

    private Map<String, MajorConfig> loadMajorConfigsFromDatabase() {
        List<XtNganhEntity> majors = majorRepository.findAll(Sort.by(Sort.Direction.ASC, "maNganh"));
        if (majors.isEmpty()) {
            return Map.of();
        }

        Map<String, MajorConfig> majorMap = new LinkedHashMap<>();
        for (XtNganhEntity major : majors) {
            String majorCode = normalize(major.getMaNganh());
            List<Combination> combos = combinationRepository.findByMajorCode(majorCode);
            List<CombinationSpec> combinations = combos.stream()
                    .filter(Objects::nonNull)
                    .map(this::toCombinationSpec)
                    .collect(Collectors.toList());

            String originalCombination = major.getToHopGoc();
            if ((originalCombination == null || originalCombination.isBlank()) && !combinations.isEmpty()) {
                originalCombination = combinations.get(0).code();
            }

            majorMap.put(majorCode, new MajorConfig(
                    majorCode,
                    major.getTenNganh() == null ? majorCode : major.getTenNganh().trim(),
                    originalCombination == null ? "" : originalCombination.trim(),
                    major.getDiemSan(),
                    major.getDiemTrungTuyen(),
                    major.getDiemSan(),
                    major.getDiemTrungTuyen(),
                    combinations
            ));
        }

        return majorMap;
    }

    private CombinationSpec toCombinationSpec(Combination combination) {
        String maToHop = normalize(combination.getMaToHop());
        String mon1 = normalizeSubjectCode(combination.getThMon1());
        String mon2 = normalizeSubjectCode(combination.getThMon2());
        String mon3 = normalizeSubjectCode(combination.getThMon3());
        double heSo1 = combination.getHsMon1() == null ? 1.0d : combination.getHsMon1().doubleValue();
        double heSo2 = combination.getHsMon2() == null ? 1.0d : combination.getHsMon2().doubleValue();
        double heSo3 = combination.getHsMon3() == null ? 1.0d : combination.getHsMon3().doubleValue();
        BigDecimal doLech = combination.getDoLech();

        return new CombinationSpec(
            maToHop,
            mon1,
            mon2,
            mon3,
            heSo1,
            heSo2,
            heSo3,
            doLech
        );
    }

    private SubjectConversion resolveConversion(Map<String, SubjectConversion> conversions, String subjectCode, List<String> warnings) {
        String normalizedCode = normalizeSubjectCode(subjectCode);
        SubjectConversion conversion = conversions.get(normalizedCode);
        if (conversion != null) {
            return conversion;
        }

        String label = subjectLabelByCode.getOrDefault(normalizedCode, normalizedCode);
        String warning = label + ": chưa có ô nhập trên form, mặc định = 0";
        if (warnings != null) {
            warnings.add(warning);
        }
        return new SubjectConversion(normalizedCode, false, 0.0d, 0.0d, warning);
    }

    private String normalizeSubjectCode(String subjectCode) {
        String value = normalize(subjectCode);
        return switch (value) {
            case "N1", "N1_THI", "N1_CC" -> "TIENG_ANH";
            case "TO" -> "TOAN";
            case "LI" -> "VAT_LY";
            case "HO" -> "HOA_HOC";
            case "SI" -> "SINH_HOC";
            case "SU" -> "LICH_SU";
            case "DI" -> "DIA_LY";
            case "VA", "VAN" -> "NGU_VAN";
            case "TI" -> "TI";
            case "CNCN" -> "CNCN";
            case "CNNN" -> "CNNN";
            case "KTPL" -> "KTPL";
            case "GD" -> "GDCD";
            default -> value;
        };
    }
    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0d : value;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private record SubjectConversion(String subjectCode, boolean provided, double rawScore, double convertedScore, String formulaText) {
    }
}

