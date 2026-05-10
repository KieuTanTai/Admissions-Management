package com.example.admissions_management.domain.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DiemCongXetTuyen {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("tsCccd")
    private String tsCccd;      // CCCD thí sinh
    @JsonProperty("maNganh")
    private String maNganh;     // Mã ngành
    @JsonProperty("maToHop")
    private String maToHop;     // Mã tổ hợp
    @JsonProperty("phuongThuc")
    private String phuongThuc;  // Phương thức xét tuyển
    @JsonProperty("diemCc")
    private BigDecimal diemCc;  // Điểm công dân
    @JsonProperty("diemUtxt")
    private BigDecimal diemUtxt; // Điểm ưu tiên theo từng thí sinh
    @JsonProperty("diemTong")
    private BigDecimal diemTong; // Tổng điểm cộng
    @JsonProperty("ghiChu")
    private String ghiChu;      // Ghi chú
    @JsonProperty("dcKeys")
    private String dcKeys;      // Khóa: cccd_manganh_matohop

    public DiemCongXetTuyen() {
    }

    public DiemCongXetTuyen(Long id, String tsCccd, String maNganh, String maToHop, 
                           String phuongThuc, BigDecimal diemCc, BigDecimal diemUtxt, 
                           BigDecimal diemTong, String ghiChu, String dcKeys) {
        this.id = id;
        this.tsCccd = tsCccd;
        this.maNganh = maNganh;
        this.maToHop = maToHop;
        this.phuongThuc = phuongThuc;
        this.diemCc = diemCc;
        this.diemUtxt = diemUtxt;
        this.diemTong = diemTong;
        this.ghiChu = ghiChu;
        this.dcKeys = dcKeys;
    }

    // Getters and Setters
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
