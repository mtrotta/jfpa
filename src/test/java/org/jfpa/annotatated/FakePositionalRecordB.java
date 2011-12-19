package org.jfpa.annotatated;

import org.jfpa.annotation.Column;
import org.jfpa.annotation.Positional;

import java.math.BigDecimal;
import java.util.Date;

@Positional
public class FakePositionalRecordB implements CommonRecord {
    @Column(length = 1)
    private String type;
    @Column(length = 6)
    private String f1;
    @Column(length = 6)
    private String f2;
    @Column(length = 10, offset = 6)
    private String f4;
    @Column(length = 10)
    private Date date;
    @Column(length = 10, dateFormat = "yyyy/MM/dd")
    private Date dateFormatted;
    @Column(length = 5)
    private Long longVal;
    @Column(length = 5)
    private BigDecimal bigDecimal;
    @Column(length = 1)
    private Boolean bool;
    @Column(length = 1, booleanFormat = {"T","F"})
    private Boolean boolFormatted;
    @Column(length = 2)
    private String blank;
    @Column(length = 4)
    private String last;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getF4() {
        return f4;
    }

    public void setF4(String f4) {
        this.f4 = f4;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDateFormatted() {
        return dateFormatted;
    }

    public void setDateFormatted(Date dateFormatted) {
        this.dateFormatted = dateFormatted;
    }

    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public Boolean getBool() {
        return bool;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    public Boolean getBoolFormatted() {
        return boolFormatted;
    }

    public void setBoolFormatted(Boolean boolFormatted) {
        this.boolFormatted = boolFormatted;
    }

    public void setBlank(String blank) {
        this.blank = blank;
    }

    public String getBlank() {
        return blank;
    }
}