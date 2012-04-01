package org.jfpa.annotatated;

import org.jfpa.annotation.TextColumn;
import org.jfpa.annotation.Delimited;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
@Delimited(delimiter = ";")
public class FakeDelimitedRecordA implements CommonRecord {
    @TextColumn(length = -1)
    private String type;
    @TextColumn(length = -1)
    private String f1;
    @TextColumn(length = -1)
    private String f2;
    @TextColumn(length = -1, offset = 1)
    private String f4;
    @TextColumn(length = -1)
    private Date date;
    @TextColumn(length = -1, dateFormat = "yyyy/MM/dd")
    private Date dateFormatted;
    @TextColumn(length = -1)
    private Long longVal;
    @TextColumn(length = -1)
    private BigDecimal bigDecimal;
    @TextColumn(length = -1)
    private Boolean bool;
    @TextColumn(length = -1, booleanFormat = {"T","F"})
    private Boolean boolFormatted;
    @TextColumn(length = -1)
    private String blank;
    @TextColumn(length = -1)
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
