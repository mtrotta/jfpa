package org.jfpa.annotatated;

import org.jfpa.utility.Formats;
import org.jfpa.utility.Utility;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
public class Common {
    public static final String posLineA = "AField1Field2      Field4    30/12/19821982/12/3010   67   YT  Last";
    public static final String posLineB = "BField1Field2      Field4    30/12/19821982/12/3010   67   YT  Last";
    public static final String posLineC = "CField1Field2      Field4    30/12/19821982/12/3010   67   YT  Last";

    public static final String delLineA = "A;Field1;Field2;;Field4;30/12/1982;1982/12/30;10;67;Y;T;;Last";
    public static final String delLineB = "B;Field1;Field2;;Field4;30/12/1982;1982/12/30;10;67;Y;T;;Last";
    public static final String delLineC = "C;Field1;Field2;;Field4;30/12/1982;1982/12/30;10;67;Y;T;;Last";

    public static final Date testDate = Utility.stringToDate("30/12/1982", Formats.DATE_FORMAT);
    public static final Long testLong = 10L;
    public static final BigDecimal testBigDecimal = new BigDecimal(67);

    public static void testRead(CommonRecord record) {
        Assert.assertEquals("Field1", record.getF1());
        Assert.assertEquals("Field2", record.getF2());
        Assert.assertEquals("Field4", record.getF4());
        Assert.assertEquals(testDate, record.getDate());
        Assert.assertEquals(testDate, record.getDateFormatted());
        Assert.assertEquals(testLong, record.getLongVal());
        Assert.assertEquals(testBigDecimal, record.getBigDecimal());
        Assert.assertEquals(Boolean.TRUE, record.getBool());
        Assert.assertEquals(Boolean.TRUE, record.getBoolFormatted());
        Assert.assertNull(record.getBlank());
        Assert.assertEquals("Last", record.getLast());
    }

    public static void fillRecord(CommonRecord record) {
        record.setF1("Field1");
        record.setF2("Field2");
        record.setF4("Field4");
        record.setDate(testDate);
        record.setDateFormatted(testDate);
        record.setLongVal(testLong);
        record.setBigDecimal(testBigDecimal);
        record.setBool(Boolean.TRUE);
        record.setBoolFormatted(Boolean.TRUE);
        record.setBlank(null);
        record.setLast("Last");
    }

}
