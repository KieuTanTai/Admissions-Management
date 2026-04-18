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
@Table(name = "xt_diemcongxetuyen", uniqueConstraints = {
        @UniqueConstraint(name = "dc_keys_UNIQUE", columnNames = "dc_keys")
})
public class XtDiemCongXetTuyenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong", nullable = false)
    private Long id;

    @Column(name = "ts_cccd", nullable = false, length = 45)
    private String tsCccd;

    @Column(name = "manganh", length = 20)
    private String maNganh;

    @Column(name = "matohop", length = 10)
    private String maToHop;

    @Column(name = "phuongthuc", length = 45)
    private String phuongThuc;

    @Column(name = "diemCC", precision = 6, scale = 2)
    private BigDecimal diemCc;

    @Column(name = "diemUtxt", precision = 6, scale = 2)
    private BigDecimal diemUtxt;

    @Column(name = "diemTong", precision = 6, scale = 2)
    private BigDecimal diemTong;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "dc_keys", nullable = false, length = 45)
    private String dcKeys;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTsCccd() {
        return tsCccd;
    }

    public void setTsCccd(String tsCccd) {
        this.tsCccd = tsCccd;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getMaToHop() {
        return maToHop;
    }

    public void setMaToHop(String maToHop) {
        this.maToHop = maToHop;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public BigDecimal getDiemCc() {
        return diemCc;
    }

    public void setDiemCc(BigDecimal diemCc) {
        this.diemCc = diemCc;
    }

    public BigDecimal getDiemUtxt() {
        return diemUtxt;
    }

    public void setDiemUtxt(BigDecimal diemUtxt) {
        this.diemUtxt = diemUtxt;
    }

    public BigDecimal getDiemTong() {
        return diemTong;
    }

    public void setDiemTong(BigDecimal diemTong) {
        this.diemTong = diemTong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getDcKeys() {
        return dcKeys;
    }

    public void setDcKeys(String dcKeys) {
        this.dcKeys = dcKeys;
    }
}
