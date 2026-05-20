package com.example.admissions_management.application.dto.response;

import java.math.BigDecimal;

public class CandidateAspirationResult {
    private String majorCode;
    private String majorName;
    private BigDecimal score;
    private String combination;
    private String method;
    private boolean admitted;
    private Integer nvThuTu;
    private String resultNote;
    public String getMajorCode() {
        return majorCode;
    }
    public String getResultNote() {
        return resultNote;
    }
    public void setResultNote(String resultNote) {
        this.resultNote = resultNote;
    }
    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }
    public String getMajorName() {
        return majorName;
    }
    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }
    public BigDecimal getScore() {
        return score;
    }
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    public String getCombination() {
        return combination;
    }
    public void setCombination(String combination) {
        this.combination = combination;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public boolean isAdmitted() {
        return admitted;
    }
    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }
    public Integer getNvThuTu() {
        return nvThuTu;
    }
    public void setNvThuTu(Integer nvThuTu) {
        this.nvThuTu = nvThuTu;
    }

    
}