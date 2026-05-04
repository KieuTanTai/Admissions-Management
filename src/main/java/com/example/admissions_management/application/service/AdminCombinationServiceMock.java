package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock service: quản lý dữ liệu tổ hợp/ngành bằng bộ nhớ (chưa kết nối DB).
 */
@Service
public class AdminCombinationServiceMock {

    private final AtomicInteger sequence = new AtomicInteger(0);
    private final Map<Integer, CombinationResponse> store = new LinkedHashMap<>();

    @PostConstruct
    void seed() {
        insert(new CombinationResponse(null, "7480101", "A00",
                "TO", (byte) 1,
                "LI", (byte) 1,
                "HO", (byte) 1,
                "7480101-A00",
                true, true, true, false, false, false, false, false, false, false, false,
                new BigDecimal("0.00")));

        insert(new CombinationResponse(null, "7480201", "D01",
                "TO", (byte) 1,
                "VA", (byte) 1,
                "NN", (byte) 1,
                "7480201-D01",
                false, true, false, false, false, true, false, false, false, false, false,
                new BigDecimal("0.50")));
    }

    public List<CombinationResponse> getAll() {
        return new ArrayList<>(store.values());
    }

    public CombinationResponse insert(CombinationResponse request) {
        Objects.requireNonNull(request, "request");
        validateRequired(request);

        CombinationResponse saved = copy(request);
        if (saved.getId() == null) {
            saved.setId(sequence.incrementAndGet());
        } else {
            // keep sequence >= id to prevent duplicates
            sequence.updateAndGet(current -> Math.max(current, saved.getId()));
        }
        if (saved.getTbKeys() == null || saved.getTbKeys().isBlank()) {
            saved.setTbKeys(buildDefaultKey(saved.getMaNganh(), saved.getMaToHop()));
        }

        if (store.values().stream().anyMatch(r -> Objects.equals(r.getTbKeys(), saved.getTbKeys()))) {
            throw new IllegalArgumentException("tb_keys already exists: " + saved.getTbKeys());
        }

        store.put(saved.getId(), saved);
        return copy(saved);
    }

    public CombinationResponse update(Integer id, CombinationResponse request) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        Objects.requireNonNull(request, "request");
        validateRequired(request);

        CombinationResponse existing = store.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Combination not found with id=" + id);
        }

        CombinationResponse updated = copy(request);
        updated.setId(id);
        if (updated.getTbKeys() == null || updated.getTbKeys().isBlank()) {
            updated.setTbKeys(buildDefaultKey(updated.getMaNganh(), updated.getMaToHop()));
        }

        // unique tb_keys check excluding itself
        if (store.values().stream().anyMatch(r -> !Objects.equals(r.getId(), id) && Objects.equals(r.getTbKeys(), updated.getTbKeys()))) {
            throw new IllegalArgumentException("tb_keys already exists: " + updated.getTbKeys());
        }

        store.put(id, updated);
        return copy(updated);
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        CombinationResponse removed = store.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("Combination not found with id=" + id);
        }
    }

    /**
     * Simple search: match by id (exact) or contains-ignore-case in maNganh/maToHop/tbKeys.
     */
    public List<CombinationResponse> find(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        String q = query.trim();
        Integer id = tryParseInt(q);

        List<CombinationResponse> results = new ArrayList<>();
        for (CombinationResponse row : store.values()) {
            if (id != null && Objects.equals(row.getId(), id)) {
                results.add(copy(row));
                continue;
            }

            String qLower = q.toLowerCase(Locale.ROOT);
            if (containsIgnoreCase(row.getMaNganh(), qLower)
                    || containsIgnoreCase(row.getMaToHop(), qLower)
                    || containsIgnoreCase(row.getTbKeys(), qLower)) {
                results.add(copy(row));
            }
        }
        return results;
    }

    private static void validateRequired(CombinationResponse request) {
        if (request.getMaNganh() == null || request.getMaNganh().isBlank()) {
            throw new IllegalArgumentException("manganh is required");
        }
        if (request.getMaToHop() == null || request.getMaToHop().isBlank()) {
            throw new IllegalArgumentException("matohop is required");
        }
    }

    private static String buildDefaultKey(String maNganh, String maToHop) {
        return (maNganh == null ? "" : maNganh.trim()) + "-" + (maToHop == null ? "" : maToHop.trim());
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean containsIgnoreCase(String value, String qLower) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(qLower);
    }

    private static CombinationResponse copy(CombinationResponse src) {
        if (src == null) {
            return null;
        }
        return new CombinationResponse(
                src.getId(),
                src.getMaNganh(),
                src.getMaToHop(),
                src.getThMon1(),
                src.getHsMon1(),
                src.getThMon2(),
                src.getHsMon2(),
                src.getThMon3(),
                src.getHsMon3(),
                src.getTbKeys(),
                src.getN1(),
                src.getTo(),
                src.getLi(),
                src.getHo(),
                src.getSi(),
                src.getVa(),
                src.getSu(),
                src.getDi(),
                src.getTi(),
                src.getKhac(),
                src.getKtpl(),
                src.getDoLech()
        );
    }
}

