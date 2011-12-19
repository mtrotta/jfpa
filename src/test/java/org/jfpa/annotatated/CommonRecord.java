package org.jfpa.annotatated;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
public interface CommonRecord {
    String getType();

    void setType(String type);

    String getF1();

    void setF1(String f1);

    String getF2();

    void setF2(String f2);

    String getF4();

    void setF4(String f4);

    Date getDate();

    void setDate(Date date);

    Date getDateFormatted();

    void setDateFormatted(Date dateFormatted);

    Long getLongVal();

    void setLongVal(Long longVal);

    String getLast();

    void setLast(String last);

    BigDecimal getBigDecimal();

    void setBigDecimal(BigDecimal bigDecimal);

    Boolean getBool();

    void setBool(Boolean bool);

    Boolean getBoolFormatted();

    void setBoolFormatted(Boolean boolFormatted);

    void setBlank(String blank);

    String getBlank();
}
