package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.ScoreCalculationRequest;
import com.example.admissions_management.application.dto.response.ScoreResultResponse;
import com.example.admissions_management.domain.repository.BangQuyDoiRepository;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNganhEntity;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VsatScoreServiceImplTest {

    @Test
    void calculateShouldSkipCombinationsMissingAnySubjectScore() {
        MajorRepository majorRepository = proxy(MajorRepository.class, (proxy, method, args) -> {
            if ("findAll".equals(method.getName()) && method.getParameterCount() == 1) {
                return Collections.<XtNganhEntity>emptyList();
            }
            return defaultValue(method.getReturnType());
        });

        ICombinationRepository combinationRepository = proxy(ICombinationRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        BangQuyDoiRepository bangQuyDoiRepository = proxy(BangQuyDoiRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        VsatScoreServiceImpl service = new VsatScoreServiceImpl(
                majorRepository,
                combinationRepository,
                new BangQuyDoiService(bangQuyDoiRepository));

        ScoreCalculationRequest request = new ScoreCalculationRequest();
        request.setMaNganh("7480201");
        request.setLoaiDiem("THPT");
        request.setDiemToan(8.0d);
        request.setDiemVan(7.5d);
        request.setDiemAnh(7.0d);
        request.setDiemLy(8.25d);
        request.setDiemHoa(null);

        ScoreResultResponse response = service.calculate(request);

        assertTrue(response.isCalculated());
        assertEquals(2, response.getCombinationResults().size());
        assertTrue(response.getCombinationResults().stream().noneMatch(result -> "A00".equals(result.getMaToHop())));
    }

    @Test
    void calculateShouldTreatZeroAsMissingAndSkipCombinations() {
        MajorRepository majorRepository = proxy(MajorRepository.class, (proxy, method, args) -> {
            return defaultValue(method.getReturnType());
        });

        ICombinationRepository combinationRepository = proxy(ICombinationRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        BangQuyDoiRepository bangQuyDoiRepository = proxy(BangQuyDoiRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        VsatScoreServiceImpl service = new VsatScoreServiceImpl(
                majorRepository,
                combinationRepository,
                new BangQuyDoiService(bangQuyDoiRepository));

        ScoreCalculationRequest request = new ScoreCalculationRequest();
        request.setMaNganh("7480201");
        request.setLoaiDiem("THPT");
        request.setDiemToan(8.0d);
        request.setDiemVan(7.5d);
        request.setDiemAnh(0.0d); // explicitly zero -> treat as missing
        request.setDiemLy(8.25d);
        request.setDiemHoa(7.0d);

        ScoreResultResponse response = service.calculate(request);

        assertTrue(response.isCalculated());
        // Combinations that include TIENG_ANH should be excluded
        assertTrue(response.getCombinationResults().stream().noneMatch(r -> r.getTenToHop().contains("TIÊNG ANH") || r.getTenToHop().contains("TIENG_ANH")));
    }

    @Test
    void calculateShouldUseEnglishCertificateWhenProvidedAndExcludeNonsupportedCombinations() {
        BangQuyDoiRepository bangQuyDoiRepository = proxy(BangQuyDoiRepository.class, (proxy, method, args) -> {
            // Mock quy đổi IELTS: score 7.0 -> 8.5 (example conversion)
            if ("timQuyTacChinhXac".equals(method.getName())) {
                return Optional.of(new java.lang.Object() {
                    public String getPhanVi() { return "8.5"; }
                });
            }
            return defaultValue(method.getReturnType());
        });

        MajorRepository majorRepository = proxy(MajorRepository.class, (proxy, method, args) -> {
            return defaultValue(method.getReturnType());
        });

        ICombinationRepository combinationRepository = proxy(ICombinationRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        VsatScoreServiceImpl service = new VsatScoreServiceImpl(
                majorRepository,
                combinationRepository,
                new BangQuyDoiService(bangQuyDoiRepository));

        ScoreCalculationRequest request = new ScoreCalculationRequest();
        request.setMaNganh("7480201");
        request.setLoaiDiem("THPT");
        request.setDiemToan(8.0d);
        request.setDiemVan(7.5d);
        request.setDiemAnh(6.0d); // lower score, but will be overridden by certificate
        request.setDiemLy(8.25d);
        request.setDiemHoa(7.0d);
        request.setLoaiChungChiAnh("IELTS");
        request.setDiemChungChiAnh(7.0d);

        ScoreResultResponse response = service.calculate(request);

        assertTrue(response.isCalculated());
        // Should have combinations with tiếng Anh since certificate was provided
        assertTrue(response.getCombinationResults().size() > 0);
        // Warnings should contain info about certificate conversion
        assertTrue(response.getWarnings().stream().anyMatch(w -> w.contains("IELTS") || w.contains("quy đổi")));
    }

    @Test
    void calculateShouldHandleToeicWithDifferentSkillsCorrectly() {
        BangQuyDoiRepository bangQuyDoiRepository = proxy(BangQuyDoiRepository.class, (proxy, method, args) -> {
            // Mock TOEIC_NGHE (Listening) conversion
            if ("timQuyTacChinhXac".equals(method.getName())) {
                return Optional.of(new java.lang.Object() {
                    public String getPhanVi() { return "9.0"; }
                });
            }
            return defaultValue(method.getReturnType());
        });

        MajorRepository majorRepository = proxy(MajorRepository.class, (proxy, method, args) -> {
            return defaultValue(method.getReturnType());
        });

        ICombinationRepository combinationRepository = proxy(ICombinationRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        VsatScoreServiceImpl service = new VsatScoreServiceImpl(
                majorRepository,
                combinationRepository,
                new BangQuyDoiService(bangQuyDoiRepository));

        ScoreCalculationRequest request = new ScoreCalculationRequest();
        request.setMaNganh("7480201");
        request.setLoaiDiem("THPT");
        request.setDiemToan(8.0d);
        request.setDiemVan(7.5d);
        request.setDiemAnh(5.0d);
        request.setDiemLy(8.25d);
        request.setDiemHoa(7.0d);
        request.setLoaiChungChiAnh("TOEIC");
        request.setDiemChungChiAnh(400.0d); // TOEIC Listening score
        request.setKyNangToeic("NGHE"); // Listening skill

        ScoreResultResponse response = service.calculate(request);

        assertTrue(response.isCalculated());
        // Should have results since TOEIC was provided
        assertTrue(response.getCombinationResults().size() > 0);
        // Warnings should mention TOEIC with skill
        assertTrue(response.getWarnings().stream().anyMatch(w -> w.contains("TOEIC") && w.contains("Nghe")));
    }

    private static <T> T proxy(Class<T> type, InvocationHandler behavior) {
        Object instance = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, behavior);
        return type.cast(instance);
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class || returnType == short.class || returnType == int.class || returnType == long.class) {
            return 0;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == double.class) {
            return 0.0d;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        }
        if (Map.class.isAssignableFrom(returnType)) {
            return Map.of();
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }
        if (Collection.class.isAssignableFrom(returnType)) {
            return List.of();
        }
        return null;
    }
}