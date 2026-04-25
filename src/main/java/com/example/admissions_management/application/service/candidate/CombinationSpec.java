package com.example.admissions_management.application.service.candidate;

public record CombinationSpec(String code, String subjectCode1, String subjectCode2, String subjectCode3) {

    public boolean containsSubject(String subjectCode) {
        return subjectCode1.equals(subjectCode)
                || subjectCode2.equals(subjectCode)
                || subjectCode3.equals(subjectCode);
    }
}
