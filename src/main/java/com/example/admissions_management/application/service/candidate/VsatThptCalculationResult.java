package com.example.admissions_management.application.service.candidate;

import java.math.BigDecimal;
import java.util.List;

public class VsatThptCalculationResult {

    private boolean calculated;
    private String message;
    private String methodType;
    private String methodLabel;
    private String scaleNote;
    private String majorName;
    private BigDecimal priorityScore;
    private BigDecimal bonusScore;
    private BigDecimal englishAppliedScore;
    private List<CombinationScoreResult> combinationResults = List.of();

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public String getScaleNote() {
        return scaleNote;
    }

    public void setScaleNote(String scaleNote) {
        this.scaleNote = scaleNote;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
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

    public BigDecimal getEnglishAppliedScore() {
        return englishAppliedScore;
    }

    public void setEnglishAppliedScore(BigDecimal englishAppliedScore) {
        this.englishAppliedScore = englishAppliedScore;
    }

    public List<CombinationScoreResult> getCombinationResults() {
        return combinationResults;
    }

    public void setCombinationResults(List<CombinationScoreResult> combinationResults) {
        this.combinationResults = combinationResults;
    }
}
