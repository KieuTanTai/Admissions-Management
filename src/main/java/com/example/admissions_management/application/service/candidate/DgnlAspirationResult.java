package com.example.admissions_management.application.service.candidate;

import java.math.BigDecimal;

public class DgnlAspirationResult {

    private String aspirationName;
    private String majorName;
    private String originalCombination;
    private BigDecimal convertedScore;
    private BigDecimal priorityScore;
    private BigDecimal bonusScore;
    private BigDecimal totalScore;
    private BigDecimal thresholdScore;
    private BigDecimal admissionScore;
    private boolean passThreshold;
    private boolean passAdmission;

    public String getAspirationName() {
        return aspirationName;
    }

    public void setAspirationName(String aspirationName) {
        this.aspirationName = aspirationName;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getOriginalCombination() {
        return originalCombination;
    }

    public void setOriginalCombination(String originalCombination) {
        this.originalCombination = originalCombination;
    }

    public BigDecimal getConvertedScore() {
        return convertedScore;
    }

    public void setConvertedScore(BigDecimal convertedScore) {
        this.convertedScore = convertedScore;
    }

    public BigDecimal getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(BigDecimal priorityScore) {
        this.priorityScore = priorityScore;
    }

    public BigDecimal getBonusScore() {
        return bonusScore;
    }

    public void setBonusScore(BigDecimal bonusScore) {
        this.bonusScore = bonusScore;
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
