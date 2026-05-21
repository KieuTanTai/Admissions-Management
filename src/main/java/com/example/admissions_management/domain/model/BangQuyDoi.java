package com.example.admissions_management.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

public class BangQuyDoi {

    private Integer id;
    private String phuongThuc;
    private String toHop;
    private String mon;
    private BigDecimal diemA;
    private BigDecimal diemB;
    private BigDecimal diemC;
    private BigDecimal diemD;
    private String maQuyDoi;
    private String phanVi;
    public BangQuyDoi() {
    }
    public BangQuyDoi(Integer id, String phuongThuc, String toHop, String mon, BigDecimal diemA, BigDecimal diemB,
            BigDecimal diemC, BigDecimal diemD, String maQuyDoi, String phanVi) {
        this.id = id;
        this.phuongThuc = phuongThuc;
        this.toHop = toHop;
        this.mon = mon;
        this.diemA = diemA;
        this.diemB = diemB;
        this.diemC = diemC;
        this.diemD = diemD;
        this.maQuyDoi = maQuyDoi;
        this.phanVi = phanVi;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getToHop() {
        return toHop;
    }

    public void setToHop(String toHop) {
        this.toHop = toHop;
    }

    public String getMon() {
        return mon;
    }

    public void setMon(String mon) {
        this.mon = mon;
    }

    public BigDecimal getDiemA() {
        return diemA;
    }

    public void setDiemA(BigDecimal diemA) {
        this.diemA = diemA;
    }

    public BigDecimal getDiemB() {
        return diemB;
    }

    public void setDiemB(BigDecimal diemB) {
        this.diemB = diemB;
    }

    public BigDecimal getDiemC() {
        return diemC;
    }

    public void setDiemC(BigDecimal diemC) {
        this.diemC = diemC;
    }

    public BigDecimal getDiemD() {
        return diemD;
    }

    public void setDiemD(BigDecimal diemD) {
        this.diemD = diemD;
    }

    public String getMaQuyDoi() {
        return maQuyDoi;
    }

    public void setMaQuyDoi(String maQuyDoi) {
        this.maQuyDoi = maQuyDoi;
    }

    public String getPhanVi() {
        return phanVi;
    }

    public void setPhanVi(String phanVi) {
        this.phanVi = phanVi;
    }
    
}
