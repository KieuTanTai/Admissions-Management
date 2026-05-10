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
@Table(name = "xt_nganh_tohop", uniqueConstraints = {
        @UniqueConstraint(name = "key_UNIQUE", columnNames = "tb_keys")
})
public class XtNganhToHopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "manganh", nullable = false, length = 45)
    private String maNganh;

    @Column(name = "matohop", nullable = false, length = 45)
    private String maToHop;

    @Column(name = "th_mon1", length = 10)
    private String mon1;

    @Column(name = "hsmon1")
    private Byte heSoMon1;

    @Column(name = "th_mon2", length = 10)
    private String mon2;

    @Column(name = "hsmon2")
    private Byte heSoMon2;

    @Column(name = "th_mon3", length = 10)
    private String mon3;

    @Column(name = "hsmon3")
    private Byte heSoMon3;

    @Column(name = "tb_keys", length = 45)
    private String tbKeys;

    @Column(name = "N1")
    private Boolean n1;

    @Column(name = "TOAN")
    private Boolean to;

    @Column(name = "LI")
    private Boolean li;

    @Column(name = "HO")
    private Boolean ho;

    @Column(name = "SI")
    private Boolean si;

    @Column(name = "VA")
    private Boolean va;

    @Column(name = "SU")
    private Boolean su;

    @Column(name = "DI")
    private Boolean di;

    @Column(name = "TI")
    private Boolean ti;

    @Column(name = "KHAC")
    private Boolean khac;

    @Column(name = "KTPL")
    private Boolean ktpl;

    @Column(name = "dolech", precision = 6, scale = 2)
    private BigDecimal doLech;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getMon1() {
        return mon1;
    }

    public void setMon1(String mon1) {
        this.mon1 = mon1;
    }

    public Byte getHeSoMon1() {
        return heSoMon1;
    }

    public void setHeSoMon1(Byte heSoMon1) {
        this.heSoMon1 = heSoMon1;
    }

    public String getMon2() {
        return mon2;
    }

    public void setMon2(String mon2) {
        this.mon2 = mon2;
    }

    public Byte getHeSoMon2() {
        return heSoMon2;
    }

    public void setHeSoMon2(Byte heSoMon2) {
        this.heSoMon2 = heSoMon2;
    }

    public String getMon3() {
        return mon3;
    }

    public void setMon3(String mon3) {
        this.mon3 = mon3;
    }

    public Byte getHeSoMon3() {
        return heSoMon3;
    }

    public void setHeSoMon3(Byte heSoMon3) {
        this.heSoMon3 = heSoMon3;
    }

    public String getTbKeys() {
        return tbKeys;
    }

    public void setTbKeys(String tbKeys) {
        this.tbKeys = tbKeys;
    }

    public Boolean getN1() {
        return n1;
    }

    public void setN1(Boolean n1) {
        this.n1 = n1;
    }

    public Boolean getTo() {
        return to;
    }

    public void setTo(Boolean to) {
        this.to = to;
    }

    public Boolean getLi() {
        return li;
    }

    public void setLi(Boolean li) {
        this.li = li;
    }

    public Boolean getHo() {
        return ho;
    }

    public void setHo(Boolean ho) {
        this.ho = ho;
    }

    public Boolean getSi() {
        return si;
    }

    public void setSi(Boolean si) {
        this.si = si;
    }

    public Boolean getVa() {
        return va;
    }

    public void setVa(Boolean va) {
        this.va = va;
    }

    public Boolean getSu() {
        return su;
    }

    public void setSu(Boolean su) {
        this.su = su;
    }

    public Boolean getDi() {
        return di;
    }

    public void setDi(Boolean di) {
        this.di = di;
    }

    public Boolean getTi() {
        return ti;
    }

    public void setTi(Boolean ti) {
        this.ti = ti;
    }

    public Boolean getKhac() {
        return khac;
    }

    public void setKhac(Boolean khac) {
        this.khac = khac;
    }

    public Boolean getKtpl() {
        return ktpl;
    }

    public void setKtpl(Boolean ktpl) {
        this.ktpl = ktpl;
    }

    public BigDecimal getDoLech() {
        return doLech;
    }

    public void setDoLech(BigDecimal doLech) {
        this.doLech = doLech;
    }
}
