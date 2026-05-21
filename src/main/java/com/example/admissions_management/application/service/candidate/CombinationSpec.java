package com.example.admissions_management.application.service.candidate;

import java.math.BigDecimal;

public record CombinationSpec(
        String code,
        String subjectCode1,
        String subjectCode2,
        String subjectCode3,
        double weight1,
        double weight2,
        double weight3,
        BigDecimal doLech) {

    public CombinationSpec(String code, String subjectCode1, String subjectCode2, String subjectCode3) {
        this(code, subjectCode1, subjectCode2, subjectCode3, 1.0d, 1.0d, 1.0d, null);
    }

    public boolean containsSubject(String subjectCode) {
        return subjectCode1.equals(subjectCode)
                || subjectCode2.equals(subjectCode)
                || subjectCode3.equals(subjectCode);
    }
}
