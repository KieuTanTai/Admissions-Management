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
@Table(name = "xt_diemthixettuyen", uniqueConstraints = {
        @UniqueConstraint(name = "cccd_UNIQUE", columnNames = "cccd")
})
public class XtDiemThiXetTuyenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi", nullable = false)
    private Integer id;

    @Column(name = "cccd", nullable = false, length = 20)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String soBaoDanh;

    @Column(name = "d_phuongthuc", length = 10)
    private String dPhuongThuc;

    @Column(name = "TOAN", precision = 8, scale = 2)
    private BigDecimal to;

    @Column(name = "LI", precision = 8, scale = 2)
    private BigDecimal li;

    @Column(name = "HO", precision = 8, scale = 2)
    private BigDecimal ho;

    @Column(name = "SI", precision = 8, scale = 2)
    private BigDecimal si;

    @Column(name = "SU", precision = 8, scale = 2)
    private BigDecimal su;

    @Column(name = "DI", precision = 8, scale = 2)
    private BigDecimal di;

    @Column(name = "VA", precision = 8, scale = 2)
    private BigDecimal va;

    @Column(name = "N1_THI", precision = 8, scale = 2)
    private BigDecimal n1Thi;

    @Column(name = "N1_CC", precision = 8, scale = 2)
    private BigDecimal n1Cc;

    @Column(name = "CNCN", precision = 8, scale = 2)
    private BigDecimal cncn;

    @Column(name = "CNNN", precision = 8, scale = 2)
    private BigDecimal cnnn;

    @Column(name = "TI", precision = 8, scale = 2)
    private BigDecimal ti;

    @Column(name = "KTPL", precision = 8, scale = 2)
    private BigDecimal ktpl;

    @Column(name = "NL1", precision = 8, scale = 2)
    private BigDecimal nl1;

    @Column(name = "NK1", precision = 8, scale = 2)
    private BigDecimal nk1;

    @Column(name = "NK2", precision = 8, scale = 2)
    private BigDecimal nk2;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoBaoDanh() {
        return soBaoDanh;
    }

    public void setSoBaoDanh(String soBaoDanh) {
        this.soBaoDanh = soBaoDanh;
    }

    public String getdPhuongThuc() {
        return dPhuongThuc;
    }

    public void setdPhuongThuc(String dPhuongThuc) {
        this.dPhuongThuc = dPhuongThuc;
    }

    public BigDecimal getTo() {
        return to;
    }

    public void setTo(BigDecimal to) {
        this.to = to;
    }

    public BigDecimal getLi() {
        return li;
    }

    public void setLi(BigDecimal li) {
        this.li = li;
    }

    public BigDecimal getHo() {
        return ho;
    }

    public void setHo(BigDecimal ho) {
        this.ho = ho;
    }

    public BigDecimal getSi() {
        return si;
    }

    public void setSi(BigDecimal si) {
        this.si = si;
    }

    public BigDecimal getSu() {
        return su;
    }

    public void setSu(BigDecimal su) {
        this.su = su;
    }

    public BigDecimal getDi() {
        return di;
    }

    public void setDi(BigDecimal di) {
        this.di = di;
    }

    public BigDecimal getVa() {
        return va;
    }

    public void setVa(BigDecimal va) {
        this.va = va;
    }

    public BigDecimal getN1Thi() {
        return n1Thi;
    }

    public void setN1Thi(BigDecimal n1Thi) {
        this.n1Thi = n1Thi;
    }

    public BigDecimal getN1Cc() {
        return n1Cc;
    }

    public void setN1Cc(BigDecimal n1Cc) {
        this.n1Cc = n1Cc;
    }

    public BigDecimal getCncn() {
        return cncn;
    }

    public void setCncn(BigDecimal cncn) {
        this.cncn = cncn;
    }

    public BigDecimal getCnnn() {
        return cnnn;
    }

    public void setCnnn(BigDecimal cnnn) {
        this.cnnn = cnnn;
    }

    public BigDecimal getTi() {
        return ti;
    }

    public void setTi(BigDecimal ti) {
        this.ti = ti;
    }

    public BigDecimal getKtpl() {
        return ktpl;
    }

    public void setKtpl(BigDecimal ktpl) {
        this.ktpl = ktpl;
    }

    public BigDecimal getNl1() {
        return nl1;
    }

    public void setNl1(BigDecimal nl1) {
        this.nl1 = nl1;
    }

    public BigDecimal getNk1() {
        return nk1;
    }

    public void setNk1(BigDecimal nk1) {
        this.nk1 = nk1;
    }

    public BigDecimal getNk2() {
        return nk2;
    }

    public void setNk2(BigDecimal nk2) {
        this.nk2 = nk2;
    }
}
