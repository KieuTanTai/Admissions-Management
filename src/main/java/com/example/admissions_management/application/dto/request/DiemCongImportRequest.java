package com.example.admissions_management.application.dto.request;

import java.math.BigDecimal;

public class DiemCongImportRequest {
    
    private Long id;          // ID (nếu file có)
    private String tsCccd;      // CCCD thí sinh
    private String maNganh;     // Mã ngành
    private String maToHop;     // Mã tổ hợp
    private String phuongThuc;  // Phương thức
    private BigDecimal diemCc;  // Điểm công dân
    private BigDecimal diemUtxt; // Điểm ưu tiên
    private BigDecimal diemTong; // Tổng điểm
    private String ghiChu;      // Ghi chú

    public DiemCongImportRequest() {
    }

    public DiemCongImportRequest(Long id, String tsCccd, String maNganh, String maToHop, String phuongThuc,
                                BigDecimal diemCc, BigDecimal diemUtxt, BigDecimal diemTong, String ghiChu) {
        this.id = id;
        this.tsCccd = tsCccd;
        this.maNganh = maNganh;
        this.maToHop = maToHop;
        this.phuongThuc = phuongThuc;
        this.diemCc = diemCc;
        this.diemUtxt = diemUtxt;
        this.diemTong = diemTong;
        this.ghiChu = ghiChu;
    }

    public DiemCongImportRequest(String tsCccd, String maNganh, String maToHop, String phuongThuc,
                                BigDecimal diemCc, BigDecimal diemUtxt, BigDecimal diemTong, String ghiChu) {
        this(null, tsCccd, maNganh, maToHop, phuongThuc, diemCc, diemUtxt, diemTong, ghiChu);
    }

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
}
