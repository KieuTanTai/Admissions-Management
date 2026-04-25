package com.example.admissions_management.presentation.web.model;

import java.math.BigDecimal;

public class DgnlCalculatorForm {

    private BigDecimal dgnlScore;
    private String majorCode;
    private String priorityObjectCode;
    private String priorityRegionCode;
    private BigDecimal bonusPoint;

    public BigDecimal getDgnlScore() {
        return dgnlScore;
    }

    public void setDgnlScore(BigDecimal dgnlScore) {
        this.dgnlScore = dgnlScore;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
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

    public BigDecimal getBonusPoint() {
        return bonusPoint;
    }

    public void setBonusPoint(BigDecimal bonusPoint) {
        this.bonusPoint = bonusPoint;
    }
}