package com.example.admissions_management.application.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ScoreResultResponse {

    private boolean calculated;
    private String message;
    private String tenNganh;
    private String loaiDiemLabel;
    private String monCongDiemLabel;
    private Double diemUuTien;
    private Double diemCongMon;
    private Double tongDiemCong;
    private List<String> warnings = new ArrayList<>();
    private List<CombinationResult> combinationResults = new ArrayList<>();

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

    public String getTenNganh() {
        return tenNganh;
    }

    public void setTenNganh(String tenNganh) {
        this.tenNganh = tenNganh;
    }

    public String getLoaiDiemLabel() {
        return loaiDiemLabel;
    }

    public void setLoaiDiemLabel(String loaiDiemLabel) {
        this.loaiDiemLabel = loaiDiemLabel;
    }

    public String getMonCongDiemLabel() {
        return monCongDiemLabel;
    }

    public void setMonCongDiemLabel(String monCongDiemLabel) {
        this.monCongDiemLabel = monCongDiemLabel;
    }

    public Double getDiemUuTien() {
        return diemUuTien;
    }

    public void setDiemUuTien(Double diemUuTien) {
        this.diemUuTien = diemUuTien;
    }

    public Double getDiemCongMon() {
        return diemCongMon;
    }

    public void setDiemCongMon(Double diemCongMon) {
        this.diemCongMon = diemCongMon;
    }

    public Double getTongDiemCong() {
        return tongDiemCong;
    }

    public void setTongDiemCong(Double tongDiemCong) {
        this.tongDiemCong = tongDiemCong;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<CombinationResult> getCombinationResults() {
        return combinationResults;
    }

    public void setCombinationResults(List<CombinationResult> combinationResults) {
        this.combinationResults = combinationResults;
    }
}