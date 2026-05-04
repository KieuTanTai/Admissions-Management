package com.example.admissions_management.domain.model;

import java.math.BigDecimal;

public class Combination {
    private Integer id;
    private String maNganh;
    private String maToHop;
    private String thMon1;
    private Byte hsMon1;
    private String thMon2;
    private Byte hsMon2;
    private String thMon3;
    private Byte hsMon3;
    private String tbKeys;
    private Boolean n1;
    private Boolean to;
    private Boolean li;
    private Boolean ho;
    private Boolean si;
    private Boolean va;
    private Boolean su;
    private Boolean di;
    private Boolean ti;
    private Boolean khac;
    private Boolean ktpl;
    private BigDecimal doLech;

    public Combination() {
    }

    public Combination(
            Integer id,
            String maNganh,
            String maToHop,
            String thMon1,
            Byte hsMon1,
            String thMon2,
            Byte hsMon2,
            String thMon3,
            Byte hsMon3,
            String tbKeys,
            Boolean n1,
            Boolean to,
            Boolean li,
            Boolean ho,
            Boolean si,
            Boolean va,
            Boolean su,
            Boolean di,
            Boolean ti,
            Boolean khac,
            Boolean ktpl,
            BigDecimal doLech
    ) {
        this.id = id;
        this.maNganh = maNganh;
        this.maToHop = maToHop;
        this.thMon1 = thMon1;
        this.hsMon1 = hsMon1;
        this.thMon2 = thMon2;
        this.hsMon2 = hsMon2;
        this.thMon3 = thMon3;
        this.hsMon3 = hsMon3;
        this.tbKeys = tbKeys;
        this.n1 = n1;
        this.to = to;
        this.li = li;
        this.ho = ho;
        this.si = si;
        this.va = va;
        this.su = su;
        this.di = di;
        this.ti = ti;
        this.khac = khac;
        this.ktpl = ktpl;
        this.doLech = doLech;
    }

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

    public String getThMon1() {
        return thMon1;
    }

    public void setThMon1(String thMon1) {
        this.thMon1 = thMon1;
    }

    public Byte getHsMon1() {
        return hsMon1;
    }

    public void setHsMon1(Byte hsMon1) {
        this.hsMon1 = hsMon1;
    }

    public String getThMon2() {
        return thMon2;
    }

    public void setThMon2(String thMon2) {
        this.thMon2 = thMon2;
    }

    public Byte getHsMon2() {
        return hsMon2;
    }

    public void setHsMon2(Byte hsMon2) {
        this.hsMon2 = hsMon2;
    }

    public String getThMon3() {
        return thMon3;
    }

    public void setThMon3(String thMon3) {
        this.thMon3 = thMon3;
    }

    public Byte getHsMon3() {
        return hsMon3;
    }

    public void setHsMon3(Byte hsMon3) {
        this.hsMon3 = hsMon3;
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
