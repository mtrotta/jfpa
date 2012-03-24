package org.jfpa.annotatated;

import org.jfpa.annotation.Column;
import org.jfpa.annotation.Delimited;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
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

    @Delimited(delimiter = ";")
    public static class BadColumns {
        @Column
        private String value1;
        @Column
        private String value2;
        @Column
        private String value3;
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadColumns() throws Exception {
        manager.read(";", BadColumns.class);
    }

    @Delimited(delimiter = ";")
    public static class FakeDelimitedLength {
        @Column(length = 3)
        private String value1;
        @Column(length = 3)
        private String value2;
        @Column(length = 5)
        private String value3;

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }

    @Test
    public void testLength() throws Exception {
        manager.loadClass(FakeDelimitedLength.class);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadLength() throws Exception {
        FakeDelimitedLength record = new FakeDelimitedLength();
        record.setValue2("long string");
        System.out.println(manager.write(record));
    }

    @Delimited(delimiter = "")
    public static class FakeDelimitedBad {}

    @Test(expected = JfpaException.class)
    public void testBad() throws Exception {
        manager.loadClass(FakeDelimitedBad.class);
    }

    @Delimited(delimiter=";", minColumns = 3)
    public static class FakeDelimitedMin {}

    @Test(expected = InvalidRecordException.class)
    public void testMin() throws Exception {
        manager.read("A;B",FakeDelimitedMin.class);
    }
}
