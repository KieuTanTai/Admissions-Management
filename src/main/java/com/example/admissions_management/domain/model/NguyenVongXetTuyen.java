package com.example.admissions_management.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NguyenVongXetTuyen {

    private Integer id;
    private String nnCccd;          // CCCD thí sinh
    private String maNganh;         // Mã ngành
    private String maToHop;         // Mã tổ hợp
    private Integer nvThuTu;        // Thứ tự nguyện vọng (1, 2, 3...)
    private BigDecimal diemThxt;    // Điểm thi xét tuyển
    private BigDecimal diemUtqd;    // Điểm ưu tiên/quy đổi
    private BigDecimal diemCong;    // Điểm cộng
    private BigDecimal diemXetTuyen; // Tổng điểm = thi + utqd + cong
    private String nvKetQua;        // Kết quả: Đậu, Trượt, Đang xét
    private String ttPhuongThuc;    // Phương thức xét tuyển
    private String ttThm;           // Trạng thái THM
    private String nvKeys;          // Khóa: cccd_manganh_matohop_tt
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NguyenVongXetTuyen() {
    }

    public NguyenVongXetTuyen(Integer id, String nnCccd, String maNganh, String maToHop,
                            Integer nvThuTu, BigDecimal diemThxt, BigDecimal diemUtqd,
                            BigDecimal diemCong, BigDecimal diemXetTuyen, String nvKetQua,
                            String ttPhuongThuc, String ttThm, String nvKeys, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nnCccd = nnCccd;
        this.maNganh = maNganh;
        this.maToHop = maToHop;
        this.nvThuTu = nvThuTu;
        this.diemThxt = diemThxt;
        this.diemUtqd = diemUtqd;
        this.diemCong = diemCong;
        this.diemXetTuyen = diemXetTuyen;
        this.nvKetQua = nvKetQua;
        this.ttPhuongThuc = ttPhuongThuc;
        this.ttThm = ttThm;
        this.nvKeys = nvKeys;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
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

    public String getNvKeys() {
        return nvKeys;
    }

    public void setNvKeys(String nvKeys) {
        this.nvKeys = nvKeys;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
