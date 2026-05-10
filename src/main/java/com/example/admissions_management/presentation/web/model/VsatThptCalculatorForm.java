package com.example.admissions_management.presentation.web.model;

import java.math.BigDecimal;

public class VsatThptCalculatorForm {

    private String methodType;
    private String majorCode;
    private BigDecimal mathScore;
    private BigDecimal literatureScore;
    private BigDecimal englishScore;
    private BigDecimal physicsScore;
    private BigDecimal chemistryScore;
    private BigDecimal biologyScore;
    private BigDecimal historyScore;
    private BigDecimal geographyScore;
    private BigDecimal civicEducationScore;
    private BigDecimal englishConvertedScore;
    private String priorityObjectCode;
    private String priorityRegionCode;
    private String bonusSubjectCode;
    private BigDecimal bonusPoint;

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public BigDecimal getMathScore() {
        return mathScore;
    }

    public void setMathScore(BigDecimal mathScore) {
        this.mathScore = mathScore;
    }

    public BigDecimal getLiteratureScore() {
        return literatureScore;
    }

    public void setLiteratureScore(BigDecimal literatureScore) {
        this.literatureScore = literatureScore;
    }

    public BigDecimal getEnglishScore() {
        return englishScore;
    }

    public void setEnglishScore(BigDecimal englishScore) {
        this.englishScore = englishScore;
    }

    public BigDecimal getPhysicsScore() {
        return physicsScore;
    }

    public void setPhysicsScore(BigDecimal physicsScore) {
        this.physicsScore = physicsScore;
    }

    public BigDecimal getChemistryScore() {
        return chemistryScore;
    }

    public void setChemistryScore(BigDecimal chemistryScore) {
        this.chemistryScore = chemistryScore;
    }

    public BigDecimal getBiologyScore() {
        return biologyScore;
    }

    public void setBiologyScore(BigDecimal biologyScore) {
        this.biologyScore = biologyScore;
    }

    public BigDecimal getHistoryScore() {
        return historyScore;
    }

    public void setHistoryScore(BigDecimal historyScore) {
        this.historyScore = historyScore;
    }

    public BigDecimal getGeographyScore() {
        return geographyScore;
    }

    public void setGeographyScore(BigDecimal geographyScore) {
        this.geographyScore = geographyScore;
    }

    public BigDecimal getCivicEducationScore() {
        return civicEducationScore;
    }

    public void setCivicEducationScore(BigDecimal civicEducationScore) {
        this.civicEducationScore = civicEducationScore;
    }

    public BigDecimal getEnglishConvertedScore() {
        return englishConvertedScore;
    }

    public void setEnglishConvertedScore(BigDecimal englishConvertedScore) {
        this.englishConvertedScore = englishConvertedScore;
    }

    public String getPriorityObjectCode() {
        return priorityObjectCode;
    }

    public void setPriorityObjectCode(String priorityObjectCode) {
        this.priorityObjectCode = priorityObjectCode;
    }

    public String getPriorityRegionCode() {
        return priorityRegionCode;
    }

    public void setPriorityRegionCode(String priorityRegionCode) {
        this.priorityRegionCode = priorityRegionCode;
    }

    public String getBonusSubjectCode() {
        return bonusSubjectCode;
    }

    public void setBonusSubjectCode(String bonusSubjectCode) {
        this.bonusSubjectCode = bonusSubjectCode;
    }

    public BigDecimal getBonusPoint() {
        return bonusPoint;
    }

    public void setBonusPoint(BigDecimal bonusPoint) {
        this.bonusPoint = bonusPoint;
    }
}