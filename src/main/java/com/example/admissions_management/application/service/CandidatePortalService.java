package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.response.CandidateAspirationResult;
import com.example.admissions_management.application.service.candidate.CandidateLookupResult;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtNguyenVongXetTuyenEntity;
import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtThiSinhXetTuyen25Entity;
import com.example.admissions_management.infrastructure.persistence.repository.CandidateRepository;
import com.example.admissions_management.infrastructure.persistence.repository.MajorRepository;
import com.example.admissions_management.infrastructure.persistence.repository.SpringDataXtNguyenVongXetTuyenRepository;
import com.example.admissions_management.presentation.web.model.CandidateLookupForm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CandidatePortalService {

    private final CandidateRepository candidateRepository;
    private final SpringDataXtNguyenVongXetTuyenRepository aspirationRepository;
    private final MajorRepository majorRepository;

    public CandidatePortalService(CandidateRepository candidateRepository,
                                  SpringDataXtNguyenVongXetTuyenRepository aspirationRepository,
                                  MajorRepository majorRepository) {
        this.candidateRepository = candidateRepository;
        this.aspirationRepository = aspirationRepository;
        this.majorRepository = majorRepository;
    }

    public CandidateLookupResult lookupResult(CandidateLookupForm form) {
        CandidateLookupResult result = new CandidateLookupResult();
        result.setSearched(true);

        String username = normalizeCandidateCode(form.getUsername());
        String inputBirthDate = normalizeBirthDate(form.getPassword());

        if (username.isBlank() || inputBirthDate.isBlank()) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Vui lòng nhập đầy đủ mã thí sinh và ngày sinh.");
            return result;
        }

        Optional<XtThiSinhXetTuyen25Entity> candidateOptional = candidateRepository.findByCccd(username);
        if (candidateOptional.isEmpty()) {
            result.setFound(false);
            result.setAdmitted(false);
            result.setMessage("Không tìm thấy thí sinh với mã đã nhập.");
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
        result.setFullName(candidate.getHo() + " " + candidate.getTen());

        // Lấy tất cả nguyện vọng
        List<XtNguyenVongXetTuyenEntity> aspirations =
                aspirationRepository.findByNnCccdOrderByNvThuTuAsc(username);

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
            .map(m -> m.getTenNganh())
            .orElse(asp.getNvMaNganh()));
    ar.setScore(round(asp.getDiemXetTuyen()));
    ar.setCombination(asp.getTtThm());
    ar.setMethod(asp.getTtPhuongThuc());
    ar.setNvThuTu(asp.getNvThuTu());

    if (admittedBefore) {
        ar.setAdmitted(false);
        ar.setResultNote("Bỏ (Đã đậu NV trước)");
    } else if (isAdmittedAspiration(asp)) {
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
        result.setMessage(admittedBefore ? "Đã tìm thấy kết quả: Trúng tuyển" : "Đã tìm thấy kết quả: Không trúng tuyển");

        return result;
    }

    private boolean isAdmittedAspiration(XtNguyenVongXetTuyenEntity aspiration) {
        if (aspiration == null || aspiration.getNvKetQua() == null) return false;
        String value = aspiration.getNvKetQua().toLowerCase().trim();
        return value.contains("trung tuyen")
                || value.contains("dat")
                || value.equals("trúng tuyển")
                || value.equals("1")
                || value.equals("true")
                || value.equals("đạt");
    }

    private String normalizeCandidateCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeBirthDate(String value) {
        if (value == null) return "";
        String digits = value.trim().replaceAll("[^0-9]", "");
        if (digits.length() != 8) return digits;
        String yyyy = digits.substring(0, 4);
        String mm = digits.substring(4, 6);
        String dd = digits.substring(6, 8);
        return dd + mm + yyyy;
    }

    private BigDecimal round(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                             : value.setScale(2, RoundingMode.HALF_UP);
    }
}