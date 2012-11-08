package org.jfpa.annotatated;

import org.jfpa.annotation.Delimited;
import org.jfpa.annotation.TextColumn;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.manager.RecordClassLoaderTest;
import org.jfpa.manager.RecordManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class DelimitedTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testRead() throws Exception {
        CommonRecord recordA = manager.read(Common.delLineA, FakeDelimitedRecordA.class);
        Common.testRead(recordA);
        CommonRecord recordB = manager.read(Common.delLineB, FakeDelimitedRecordB.class);
        Common.testRead(recordB);
    }

    @Test
    public void testWrite() throws Exception {
        FakeDelimitedRecordA record = new FakeDelimitedRecordA();
        Common.fillRecord(record);
        record.setType("A");
        Assert.assertEquals(Common.delLineA, manager.write(record));
    }

    @Test
    public void testEnclose() throws Exception {
        String expected = "\"ABC\";\"DEF\"";
        Enclosed record = new Enclosed();
        record.value1 = "ABC";
        record.value2 = "DEF";
        Assert.assertEquals(expected, manager.write(record));
        record = manager.read(expected, Enclosed.class);
        Assert.assertEquals("ABC", record.value1);
        Assert.assertEquals("DEF", record.value2);
        String empty = "\"\";\"DEF\"";
        record = manager.read(empty, Enclosed.class);
        Assert.assertNull(record.value1);
        String not = "ABC;\"DEF\"";
        try {
            manager.read(not, Enclosed.class);
            Assert.fail();
        } catch (InvalidRecordException ignore) {}
        String bad = "\"ABC;DEF";
        try {
            manager.read(bad, Enclosed.class);
            Assert.fail();
        } catch (InvalidRecordException ignore) {}
        String bad2 = "ABC\";DEF";
        try {
            manager.read(bad2, Enclosed.class);
            Assert.fail();
        } catch (InvalidRecordException ignore) {}
        String bad3 = "\";DEF";
        try {
            manager.read(bad3, Enclosed.class);
            Assert.fail();
        } catch (InvalidRecordException ignore) {}
    }

    @Delimited(delimiter = ";", stringEnclose = "\"")
    public static class Enclosed {
        @TextColumn(length = -1)
        public String value1;
        @TextColumn(length = -1)
        public String value2;
    }

    @Delimited(delimiter = ";")
    public static class BadColumns {
        @TextColumn(length = -1)
        private String value1;
        @TextColumn(length = -1)
        private String value2;
        @TextColumn(length = -1)
        private String value3;
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadColumns() throws Exception {
        manager.read(";", BadColumns.class);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadLength() throws Exception {
        RecordClassLoaderTest.FakeDelimitedLength record = new RecordClassLoaderTest.FakeDelimitedLength();
        record.setValue2("long string");
        System.out.println(manager.write(record));
    }

    @Delimited(delimiter=";", minColumns = 3)
    public static class FakeDelimitedMin {}

    @Test(expected = InvalidRecordException.class)
    public void testMin() throws Exception {
        manager.read("A;B",FakeDelimitedMin.class);
    }
}
