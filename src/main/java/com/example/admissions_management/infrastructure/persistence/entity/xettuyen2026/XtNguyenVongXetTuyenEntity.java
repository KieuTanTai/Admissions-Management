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
@Table(name = "xt_nguyenvongxettuyen", uniqueConstraints = {
        @UniqueConstraint(name = "nv_keys_UNIQUE", columnNames = "nv_keys")
})
public class XtNguyenVongXetTuyenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv", nullable = false)
    private Integer id;

    @Column(name = "nn_cccd", nullable = false, length = 45)
    private String nnCccd;

    @Column(name = "nv_manganh", nullable = false, length = 45)
    private String nvMaNganh;

    @Column(name = "nv_tt", nullable = false)
    private Integer nvThuTu;

    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private BigDecimal diemThxt;

    @Column(name = "diem_utqd", precision = 10, scale = 5)
    private BigDecimal diemUtqd;

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private BigDecimal diemCong;

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private BigDecimal diemXetTuyen;

    @Column(name = "nv_ketqua", length = 45)
    private String nvKetQua;

    @Column(name = "nv_keys", length = 45)
    private String nvKeys;

    @Column(name = "tt_phuongthuc", length = 45)
    private String ttPhuongThuc;

    @Column(name = "tt_thm", length = 45)
    private String ttThm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNnCccd() {
        return nnCccd;
    }

    public void setNnCccd(String nnCccd) {
        this.nnCccd = nnCccd;
    }

    public String getNvMaNganh() {
        return nvMaNganh;
    }

    public void setNvMaNganh(String nvMaNganh) {
        this.nvMaNganh = nvMaNganh;
    }

    public Integer getNvThuTu() {
        return nvThuTu;
    }

    public void setNvThuTu(Integer nvThuTu) {
        this.nvThuTu = nvThuTu;
    }

    public BigDecimal getDiemThxt() {
        return diemThxt;
    }

    public void setDiemThxt(BigDecimal diemThxt) {
        this.diemThxt = diemThxt;
    }

    public BigDecimal getDiemUtqd() {
        return diemUtqd;
    }

    public void setDiemUtqd(BigDecimal diemUtqd) {
        this.diemUtqd = diemUtqd;
    }

    public BigDecimal getDiemCong() {
        return diemCong;
    }

    public void setDiemCong(BigDecimal diemCong) {
        this.diemCong = diemCong;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public void setDiemXetTuyen(BigDecimal diemXetTuyen) {
        this.diemXetTuyen = diemXetTuyen;
    }

    public String getNvKetQua() {
        return nvKetQua;
    }

    public void setNvKetQua(String nvKetQua) {
        this.nvKetQua = nvKetQua;
    }

    public String getNvKeys() {
        return nvKeys;
    }

    public void setNvKeys(String nvKeys) {
        this.nvKeys = nvKeys;
    }

    public String getTtPhuongThuc() {
        return ttPhuongThuc;
    }

    public void setTtPhuongThuc(String ttPhuongThuc) {
        this.ttPhuongThuc = ttPhuongThuc;
    }

    public String getTtThm() {
        return ttThm;
    }

    public void setTtThm(String ttThm) {
        this.ttThm = ttThm;
    }
}
