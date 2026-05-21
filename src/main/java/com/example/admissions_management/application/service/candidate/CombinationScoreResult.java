package com.example.admissions_management.application.service.candidate;

import java.math.BigDecimal;

public class CombinationScoreResult {

    private String combinationCode;
    private String subjectFormula;
    private BigDecimal totalScore;
    private BigDecimal thresholdScore;
    private BigDecimal admissionScore;
    private boolean passThreshold;
    private boolean passAdmission;

    public String getCombinationCode() {
        return combinationCode;
    }

    public void setCombinationCode(String combinationCode) {
        this.combinationCode = combinationCode;
    }

    public String getSubjectFormula() {
        return subjectFormula;
    }

    public void setSubjectFormula(String subjectFormula) {
        this.subjectFormula = subjectFormula;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getThresholdScore() {
        return thresholdScore;
    }

    public void setThresholdScore(BigDecimal thresholdScore) {
        this.thresholdScore = thresholdScore;
    }

    public BigDecimal getAdmissionScore() {
        return admissionScore;
    }

    public void setAdmissionScore(BigDecimal admissionScore) {
        this.admissionScore = admissionScore;
    }

    public boolean isPassThreshold() {
        return passThreshold;
    }

    public void setPassThreshold(boolean passThreshold) {
        this.passThreshold = passThreshold;
    }

    public boolean isPassAdmission() {
        return passAdmission;
    }

    public void setPassAdmission(boolean passAdmission) {
        this.passAdmission = passAdmission;
    }
}
