package org.jfpa.annotatated;

import org.jfpa.annotation.Binary;
import org.jfpa.annotation.Column;
import org.jfpa.annotation.Delimited;
import org.jfpa.annotation.MultipleDelimited;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.manager.BinaryRecordManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 16/10/11
 */
public class BinaryTest {

    private final String val1 = "0123456";
    private final String val2 = "abc";
    private final String line = val1+":"+val2;

    private int complete;

    private RecordHandler<String> stringHandler = new RecordHandler<String>() {
        public void handle(String record) {
            Assert.assertEquals(line, record);
            complete++;
        }
    };

    @Before
    public void setUp() throws Exception {
        complete = 0;
    }

    @Test
    public void testString() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(stringHandler, FakeBinaryRecordBytes.class);
        manager.process(line.getBytes());
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Test
    public void testStringOutOfSync() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(stringHandler, FakeBinaryRecordBytes.class);
        final String junk = "123";
        try {
            manager.process((junk + line + line + line).getBytes());
            Assert.fail();
        } catch (InvalidRecordException e) {
            Assert.assertEquals(junk, e.getRecord());
        }
        manager.flush();
        Assert.assertEquals(3, complete);
    }

    @Binary(patternString = "012")
    public static class FakeBinaryRecordString {
    }

    @Test
    public void testPatternString() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(stringHandler, FakeBinaryRecordString.class);
        manager.process(line.getBytes());
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Binary(patternString = "012", encoding = "Cp500")
    public static class FakeBinaryRecordEncoding {
    }

    @Test
    public void testEncoding() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(stringHandler, FakeBinaryRecordEncoding.class);
        manager.process(line.getBytes("Cp500"));
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Binary(patternString = "012", encoding = "BadOne")
    public static class BadBinaryRecordEncodingString {
    }

    @Test(expected = JfpaException.class)
    public void testBadEncodingString() throws Exception {
        new BinaryRecordManager(stringHandler, BadBinaryRecordEncodingString.class);
    }

    @Binary(patternString = "012", encoding = "BadOne")
    @Delimited(delimiter = ";")
    public static class BadBinaryRecordEncoding {
    }

    @Test(expected = JfpaException.class)
    public void testBadEncoding() throws Exception {
        new BinaryRecordManager(BadBinaryRecordEncoding.class, null);
    }

    private RecordHandler<FakeBinaryRecordBytes> handler = new RecordHandler<FakeBinaryRecordBytes>() {
        public void handle(FakeBinaryRecordBytes record) {
            complete++;
            Assert.assertEquals(val1, record.getVal1());
            Assert.assertEquals(val2, record.getVal2());
        }
    };

    @Test
    public void test() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(FakeBinaryRecordBytes.class, handler);
        manager.process(line.getBytes());
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Binary(pattern = {0x30, 0x31, 0x32})
    @Delimited(delimiter = ":")
    public static class FakeBinaryRecordBytes {
        @Column
        private String val1;
        @Column
        private String val2;

        public String getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }
    }

    @Binary(pattern = {(byte)0xF0, (byte)0xF1, (byte)0xF2}, encoding = "Cp500")
    @Delimited(delimiter = ":")
    public static class FakeBinaryRecordBytesEncoding {
        @Column
        private String val1;
        @Column
        private String val2;

        public String getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }
    }

    private RecordHandler<FakeBinaryRecordBytesEncoding> handlerEncoding = new RecordHandler<FakeBinaryRecordBytesEncoding>() {
        public void handle(FakeBinaryRecordBytesEncoding record) {
            complete++;
            Assert.assertEquals(val1, record.getVal1());
            Assert.assertEquals(val2, record.getVal2());
        }
    };

    @Test
    public void testDelEncoding() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(FakeBinaryRecordBytesEncoding.class, handlerEncoding);
        manager.process(line.getBytes("Cp500"));
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Binary(patternString = "012", encoding = "Cp500")
    @Delimited(delimiter = ":")
    public static class FakeBinaryRecordBytesEncodingString {
        @Column
        private String val1;
        @Column
        private String val2;

        public String getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }
    }

    private RecordHandler<FakeBinaryRecordBytesEncodingString> handlerEncodingString = new RecordHandler<FakeBinaryRecordBytesEncodingString>() {
        public void handle(FakeBinaryRecordBytesEncodingString record) {
            complete++;
            Assert.assertEquals(val1, record.getVal1());
            Assert.assertEquals(val2, record.getVal2());
        }
    };

    @Test
    public void testDelEncodingString() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(FakeBinaryRecordBytesEncodingString.class, handlerEncodingString);
        manager.process(line.getBytes("Cp500"));
        manager.flush();
        Assert.assertEquals(1, complete);
    }

    @Binary
    @Delimited(delimiter = ":")
    public static class FakeBinaryRecordBadMissingBoth {
    }

    @Test(expected = JfpaException.class)
    public void testBadMissingBoth() throws Exception {
        new BinaryRecordManager(FakeBinaryRecordBadMissingBoth.class, null);
    }

    @Binary(pattern = {0x1}, patternString = "ABC")
    @Delimited(delimiter = ";")
    public static class FakeBinaryRecordBadBoth {
    }

    @Test(expected = JfpaException.class)
    public void testBadBoth() throws Exception {
        new BinaryRecordManager(FakeBinaryRecordBadBoth.class, null);
    }

    @Delimited(delimiter = ";")
    public static class FakeBinaryRecordBadMissingBinary {
    }

    @Test(expected = JfpaException.class)
    public void testBadMissingBinary() throws Exception {
        new BinaryRecordManager(FakeBinaryRecordBadMissingBinary.class, null);
    }

    @Binary(patternString = "012")
    public static class FakeBinaryRecordBadMissingDelimited {
    }

    @Test(expected = JfpaException.class)
    public void testBadMissingDelimited() throws Exception {
        new BinaryRecordManager(FakeBinaryRecordBadMissingDelimited.class, null);
    }

    @Delimited(delimiter = ";")
    public static class BadBinaryRecord {
    }

    @Test(expected = JfpaException.class)
    public void testBad() throws Exception {
        new BinaryRecordManager(BadBinaryRecord.class, null);
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class BadMultipleBinaryRecord {
    }

    @Test(expected = JfpaException.class)
    public void testBadMultiple() throws Exception {
        new BinaryRecordManager(BadMultipleBinaryRecord.class, null);
    }

    @Test(expected = JfpaException.class)
    public void testBadMultipleString() throws Exception {
        new BinaryRecordManager(null, BadMultipleBinaryRecord.class);
    }

    private RecordHandler<FakeBinaryRecordBytes> badHandler = new RecordHandler<FakeBinaryRecordBytes>() {
        public void handle(FakeBinaryRecordBytes record) {
            throw new RuntimeException("Simulated exception");
        }
    };

    @Test(expected = RuntimeException.class)
    public void testBadHandler() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(FakeBinaryRecordBytes.class, badHandler);
        manager.process(line.getBytes());
        manager.flush();
    }

    private RecordHandler<String> badStringHandler = new RecordHandler<String>() {
        public void handle(String string) {
            throw new RuntimeException("Simulated exception");
        }
    };

    @Test(expected = RuntimeException.class)
    public void testBadStringHandler() throws Exception {
        BinaryRecordManager manager = new BinaryRecordManager(badStringHandler, FakeBinaryRecordBytes.class);
        manager.process(line.getBytes());
        manager.flush();
    }
}
