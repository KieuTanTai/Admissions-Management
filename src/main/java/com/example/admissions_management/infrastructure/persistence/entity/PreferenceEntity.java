package com.example.admissions_management.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "preference")
public class PreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maNganh;
    private String phuongThuc; // THPT, ĐGNL, V-SAT
    private Integer soLuong;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMaNganh() {
        return maNganh;
    }
    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }
    public String getPhuongThuc() {
        return phuongThuc;
    }
    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }
    public Integer getSoLuong() {
        return soLuong;
    }
    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    
}