package com.example.admissions_management.application.service.candidate;

import java.util.List;

public class DgnlCalculationResult {

    private boolean calculated;
    private String message;
    private String majorName;
    private List<DgnlAspirationResult> rows = List.of();

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

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public List<DgnlAspirationResult> getRows() {
        return rows;
    }

    public void setRows(List<DgnlAspirationResult> rows) {
        this.rows = rows;
    }
}
