package com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "xt_bangquydoi", uniqueConstraints = {
        @UniqueConstraint(name = "d_maquydoi_UNIQUE", columnNames = "d_maquydoi")
})
public class XtBangQuyDoiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd", nullable = false)
    private Integer id;

    @Column(name = "d_phuongthuc", length = 45)
    private String phuongThuc;

    @Column(name = "d_tohop", length = 45)
    private String toHop;

    @Column(name = "d_mon", length = 45)
    private String mon;

    @Column(name = "d_diema", precision = 6, scale = 2)
    private BigDecimal diemA;

    @Column(name = "d_diemb", precision = 6, scale = 2)
    private BigDecimal diemB;

    @Column(name = "d_diemc", precision = 6, scale = 2)
    private BigDecimal diemC;

    @Column(name = "d_diemd", precision = 6, scale = 2)
    private BigDecimal diemD;

    @Column(name = "d_maquydoi", length = 45)
    private String maQuyDoi;

    @Column(name = "d_phanvi", length = 45)
    private String phanVi;

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
