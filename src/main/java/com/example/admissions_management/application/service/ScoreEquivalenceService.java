package com.example.admissions_management.application.service;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtDiemThiXetTuyenEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ScoreEquivalenceService {

    private static final String METHOD_THPT = "THPT";
    private static final String METHOD_VSAT = "VSAT";
    private static final String METHOD_DGNL = "DGNL";

    private final BangQuyDoiService bangQuyDoiService;

    public ScoreEquivalenceService(BangQuyDoiService bangQuyDoiService) {
        this.bangQuyDoiService = bangQuyDoiService;
    }

    public void refreshEquivalence(XtDiemThiXetTuyenEntity entity) {
        if (entity == null) {
            return;
        }

        BigDecimal thptEquivalent = calculateThptEquivalent(entity);
        BigDecimal vsatEquivalent = calculateVsatEquivalent(entity);
        BigDecimal dgnlEquivalent = calculateDgnlEquivalent(entity);

        entity.setThptEquivalentScore(thptEquivalent);
        entity.setVsatEquivalentScore(vsatEquivalent);
        entity.setDgnlEquivalentScore(dgnlEquivalent);

        BestScore bestScore = chooseBestScore(thptEquivalent, vsatEquivalent, dgnlEquivalent);
        entity.setBestEquivalentMethod(bestScore.method());
        entity.setBestEquivalentScore(bestScore.score());
    }

    private BigDecimal calculateThptEquivalent(XtDiemThiXetTuyenEntity entity) {
        List<BigDecimal> scores = new ArrayList<>();
        addIfEligible(scores, entity.getTo(), true);
        addIfEligible(scores, entity.getLi(), true);
        addIfEligible(scores, entity.getHo(), true);
        addIfEligible(scores, entity.getSi(), true);
        addIfEligible(scores, entity.getSu(), true);
        addIfEligible(scores, entity.getDi(), true);
        addIfEligible(scores, entity.getVa(), true);
        addIfEligible(scores, entity.getN1Thi(), true);
        addIfEligible(scores, entity.getN1Cc(), true);
        addIfEligible(scores, entity.getCncn(), true);
        addIfEligible(scores, entity.getCnnn(), true);
        addIfEligible(scores, entity.getTi(), true);
        addIfEligible(scores, entity.getKtpl(), true);
        addIfEligible(scores, entity.getNk1(), true);
        addIfEligible(scores, entity.getNk2(), true);

        return sumTopThree(scores);
    }

    private BigDecimal calculateVsatEquivalent(XtDiemThiXetTuyenEntity entity) {
        List<SubjectScore> scores = new ArrayList<>();
        addIfEligible(scores, "TOAN", entity.getTo());
        addIfEligible(scores, "VAT_LY", entity.getLi());
        addIfEligible(scores, "HOA_HOC", entity.getHo());
        addIfEligible(scores, "SINH_HOC", entity.getSi());
        addIfEligible(scores, "LICH_SU", entity.getSu());
        addIfEligible(scores, "DIA_LY", entity.getDi());
        addIfEligible(scores, "NGU_VAN", entity.getVa());
        addIfEligible(scores, "TIENG_ANH", entity.getN1Thi());
        addIfEligible(scores, "TIENG_ANH", entity.getN1Cc());

        List<BigDecimal> converted = new ArrayList<>();
        for (SubjectScore score : scores) {
            BigDecimal value = score.rawScore();
            if (value.compareTo(BigDecimal.TEN) <= 0) {
                continue;
            }

            BigDecimal convertedScore = convertVsatScore(score.subjectCode(), value);
            if (convertedScore != null) {
                converted.add(convertedScore);
            }
        }

        return sumTopThree(converted);
    }

    private BigDecimal calculateDgnlEquivalent(XtDiemThiXetTuyenEntity entity) {
        BigDecimal raw = entity.getNl1();
        if (raw == null || raw.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        try {
            Map<String, String> result = bangQuyDoiService.quyDoiDiemKhaoThi(METHOD_DGNL, METHOD_DGNL, raw);
            if (result != null) {
                String value = result.get("diemQuyDoi");
                if (value != null && !value.isBlank()) {
                    return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ignored) {
            // Fallback below.
        }

        return raw.divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertVsatScore(String subjectCode, BigDecimal rawScore) {
        try {
            Map<String, String> result = bangQuyDoiService.quyDoiDiemKhaoThi(METHOD_VSAT, subjectCode, rawScore);
            if (result != null) {
                String value = result.get("diemQuyDoi");
                if (value != null && !value.isBlank()) {
                    return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ignored) {
            // Fallback below.
        }

        return rawScore.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
    }

    private void addIfEligible(List<BigDecimal> scores, BigDecimal value, boolean thptScale) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (thptScale && value.compareTo(BigDecimal.TEN) <= 0) {
            scores.add(value.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private void addIfEligible(List<SubjectScore> scores, String subjectCode, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        scores.add(new SubjectScore(subjectCode, value));
    }

    private BigDecimal sumTopThree(List<BigDecimal> scores) {
        if (scores.isEmpty()) {
            return null;
        }

        return scores.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BestScore chooseBestScore(BigDecimal thptEquivalent, BigDecimal vsatEquivalent, BigDecimal dgnlEquivalent) {
        BestScore best = new BestScore(null, null);
        best = compare(best, METHOD_THPT, thptEquivalent);
        best = compare(best, METHOD_VSAT, vsatEquivalent);
        best = compare(best, METHOD_DGNL, dgnlEquivalent);
        return best;
    }

    private BestScore compare(BestScore currentBest, String method, BigDecimal score) {
        if (score == null) {
            return currentBest;
        }

        if (currentBest.score() == null || score.compareTo(currentBest.score()) > 0) {
            return new BestScore(method, score);
        }

        return currentBest;
    }

    private record SubjectScore(String subjectCode, BigDecimal rawScore) {
    }

    private record BestScore(String method, BigDecimal score) {
    }
}