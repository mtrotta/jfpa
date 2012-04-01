package org.jfpa.utility;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 26/10/11
 */
public class UtilityTest {
    @Test
    public void test() throws Exception {
        Assert.assertEquals("A;B", Utility.buildDelimitedString(";", new String[]{"A", "B"}));
    }

    @Test
    public void testString() throws Exception {
        Assert.assertNotNull(Utility.getValidString(null));
        Assert.assertNull(Utility.substring(null, 3, 4, false));
        Assert.assertNull(Utility.substring("", 3, 4, false));
        Assert.assertNull(Utility.substring("A", 3, 4, false));
        Assert.assertEquals("A", Utility.substring("A", 0, 4, false));
        int len = 3;
        Assert.assertTrue(len == Utility.rightPad(null, len).length());
        Assert.assertEquals("", Utility.buildDelimitedString(";", null));
        Assert.assertEquals("", Utility.buildDelimitedString(";", new String[]{null}));
        Assert.assertEquals("", Utility.buildDelimitedString(";", new String[]{}));
        Assert.assertEquals(Utility.NEW_LINE+ Utility.NEW_LINE, Utility.buildNewLineString(Arrays.asList("", null, "")));
        List<String> list1 = Arrays.asList("", null);
        List<List<String>> all = new ArrayList<List<String>>();
        all.add(list1);
        all.add(null);
        Assert.assertEquals(Utility.NEW_LINE+ Utility.NEW_LINE, Utility.buildNewLineStringFromList(all));
    }

    @Test
    public void testDate() throws Exception {
        Assert.assertNull(Utility.dateToString(null, ""));
    }

    @Test
    public void testInteger() throws Exception {
        Assert.assertNull(Utility.stringToInteger(null));
        Assert.assertNull(Utility.stringToInteger(""));
        Assert.assertNull(Utility.integerToString(null));
        Assert.assertNotNull(Utility.stringToInteger("+2"));
    }

    @Test
    public void testLong() throws Exception {
        Assert.assertNull(Utility.stringToLong(null));
        Assert.assertNull(Utility.stringToLong(""));
        Assert.assertNull(Utility.longToString(null));
        Assert.assertNotNull(Utility.stringToLong("+2"));
    }

    @Test
    public void testDouble() throws Exception {
        Assert.assertNull(Utility.stringToDouble(null));
        Assert.assertNull(Utility.stringToDouble(""));
        Assert.assertNull(Utility.doubleToString(null));
        Assert.assertNotNull(Utility.stringToDouble("+2"));
    }

    @Test
    public void testBigDecimal() throws Exception {
        Assert.assertNull(Utility.stringToBigDecimal(""));
        Assert.assertNull(Utility.bigDecimalToString(null));
    }

    @Test
    public void testBoolean() throws Exception {
        String[] trueFalse = new String[] {"T","F"};
        Assert.assertEquals(trueFalse[0], Utility.booleanToString(true, trueFalse));
        Assert.assertEquals(trueFalse[1], Utility.booleanToString(false, trueFalse));
        Assert.assertNull(Utility.booleanToString(null, trueFalse));
    }

    @Test
    public void testCheckDomain() throws Exception {
        Assert.assertFalse(Utility.checkDomain(null, "A", "B", "C"));
        Assert.assertTrue(Utility.checkDomain("B", "A", "B", "C"));
        Assert.assertFalse(Utility.checkDomain("Z", "A", "B", "C"));
    }

    @Test
    public void testContainAnyValues() throws Exception {
        Assert.assertTrue(Utility.containsAnyValues(null, ""));
        Assert.assertFalse(Utility.containsAnyValues());
        Assert.assertFalse(Utility.containsAnyValues(null, null));
    }

    @Test
    public void testContainsAllValues() throws Exception {
        Assert.assertTrue(Utility.containsAllValues("", ""));
        Assert.assertTrue(Utility.containsAllValues());
        Assert.assertFalse(Utility.containsAllValues("", null));
    }

    @Test
    public void testEnclose() throws Exception {
        Assert.assertEquals("\"ABC\"", Utility.encloseString("ABC","\""));
        Assert.assertEquals("\"\"", Utility.encloseString(null,"\""));
    }

    @Test
    public void testUnenclose() throws Exception {
        Assert.assertNull(Utility.unencloseString(null, "\""));
        Assert.assertEquals("123", Utility.unencloseString("\"123\"", "\""));
        Assert.assertEquals("", Utility.unencloseString("\"\"", "\""));
        try {
            Utility.unencloseString("\"abc", "\"");
        } catch (IllegalArgumentException ignore) {}
        try {
            Utility.unencloseString("", "\"");
        } catch (IllegalArgumentException ignore) {}
        try {
            Utility.unencloseString("abc\"", "\"");
        } catch (IllegalArgumentException ignore) {}
    }
}
