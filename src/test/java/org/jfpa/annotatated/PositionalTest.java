package org.jfpa.annotatated;

import org.jfpa.annotation.Positional;
import org.jfpa.annotation.TextColumn;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.manager.RecordManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class PositionalTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testRead() throws Exception {
        CommonRecord record = manager.read(Common.posLineA, FakePositionalRecordA.class);
        Common.testRead(record);
    }

    @Test
    public void testWrite() throws Exception {
        CommonRecord record = new FakePositionalRecordA();
        Common.fillRecord(record);
        record.setType("A");
        Assert.assertEquals(Common.posLineA, manager.write(record));
    }

    @Positional
    public static class FakePositionalLength {
        @TextColumn(length = 3)
        private String value1;
        @TextColumn(length = 3)
        private String value2;
        @TextColumn(length = 5)
        private String value3;
    }

    @Test(expected = InvalidRecordException.class)
    public void testTooShort() throws Exception {
        manager.read("Short", FakePositionalLength.class);
    }

    @Positional(minLength = 10)
    public static class FakePositionalMin {
    }

    @Test(expected = InvalidRecordException.class)
    public void testMinLength() throws Exception {
        manager.read("ABC", FakePositionalMin.class);
    }

    @Positional(minLength = 10)
    public static class FakePositionalMinUseless {
        @TextColumn(length = 15)
        private String val;
    }

    @Test(expected = InvalidRecordException.class)
    public void testMinLengthUseless() throws Exception {
        manager.read("ABC", FakePositionalMinUseless.class);
    }
}
