package org.jfpa.annotatated;

import org.jfpa.annotation.MultipleDelimited;
import org.jfpa.annotation.SubRecord;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.MultipleRecordValidator;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.manager.RecordManager;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class MultipleDelimitedTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testReadA() throws Exception {
        FakeMultipleDelimitedRecord record = manager.read(Common.delLineA, FakeMultipleDelimitedRecord.class);
        Assert.assertNotNull(record.getRecordA());
        Assert.assertNull(record.getRecordB());
        Common.testRead(record.getRecordA());
    }

    @Test
    public void testReadB() throws Exception {
        FakeMultipleDelimitedRecord record = manager.read(Common.delLineB, FakeMultipleDelimitedRecord.class);
        Assert.assertNull(record.getRecordA());
        Assert.assertNotNull(record.getRecordB());
        Common.testRead(record.getRecordB());
    }

    @Test(expected = InvalidRecordException.class)
    public void testReadC() throws Exception {
        manager.read(Common.delLineC, FakeMultipleDelimitedRecord.class);
    }

    @Test
    public void testWrite() throws Exception {
        FakeMultipleDelimitedRecord record = new FakeMultipleDelimitedRecord();
        record.setRecordA(manager.read(Common.delLineA, FakeDelimitedRecordA.class));
        Assert.assertEquals(Common.delLineA, manager.write(record));
    }

    @Test(expected = InvalidRecordException.class)
    public void testWriteFail() throws Exception {
        FakeMultipleDelimitedRecord record = new FakeMultipleDelimitedRecord();
        manager.write(record);
    }

    @Test
    public void testWriteBoth() throws Exception {
        FakeMultipleDelimitedRecord record = new FakeMultipleDelimitedRecord();
        record.setRecordA(manager.read(Common.delLineA, FakeDelimitedRecordA.class));
        record.setRecordB(manager.read(Common.delLineB, FakeDelimitedRecordB.class));
        Assert.assertEquals(Utility.buildNewLineString(Common.delLineA, Common.delLineB), manager.write(record));
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class FakeMultipleDelimitedRecord {
        @SubRecord(type = "A")
        private FakeDelimitedRecordA recordA;
        @SubRecord(type = "B")
        private FakeDelimitedRecordB recordB;

        public FakeDelimitedRecordA getRecordA() {
            return recordA;
        }

        public void setRecordA(FakeDelimitedRecordA recordA) {
            this.recordA = recordA;
        }

        public FakeDelimitedRecordB getRecordB() {
            return recordB;
        }

        public void setRecordB(FakeDelimitedRecordB recordB) {
            this.recordB = recordB;
        }
    }

    @MultipleDelimited
    public static class FakeMultipleDelimitedRecordExtractor implements TypeExtractor {
        @SubRecord(type = "A")
        private FakeDelimitedRecordA recordA;
        @SubRecord(type = "B")
        private FakeDelimitedRecordB recordB;

        public FakeDelimitedRecordA getRecordA() {
            return recordA;
        }

        public FakeDelimitedRecordB getRecordB() {
            return recordB;
        }

        public String extractType(String line) {
            return Utility.substring(line, 0, 1);
        }
    }

    @Test
    public void testExtractor() throws Exception {
        FakeMultipleDelimitedRecordExtractor record = manager.read(Common.delLineA, FakeMultipleDelimitedRecordExtractor.class);
        Assert.assertNotNull(record.getRecordA());
        Assert.assertNull(record.getRecordB());
        Common.testRead(record.getRecordA());
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class FakeMultipleDelimitedRecordValidator implements MultipleRecordValidator {
        @SubRecord(type = "A")
        private FakeDelimitedRecordA recordA;
        @SubRecord(type = "B")
        private List<FakeDelimitedRecordB> recordB;

        public void setRecordB(List<FakeDelimitedRecordB> recordB) {
            this.recordB = recordB;
        }

        public void validate() throws InvalidMultipleRecordException {
            if (recordB == null || recordB.isEmpty()) {
                throw new InvalidMultipleRecordException("Simulated exception");
            }
        }
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testValidatorRead() throws Exception {
        manager.read(Common.delLineA, FakeMultipleDelimitedRecordValidator.class);
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testValidatorWrite() throws Exception {
        FakeMultipleDelimitedRecordValidator record = new FakeMultipleDelimitedRecordValidator();
        record.setRecordB(new ArrayList<FakeDelimitedRecordB>());
        manager.write(record);
    }

    @MultipleDelimited(delimiter = ";", typePosition = 10)
    public static class BadMultipleDelimitedRecordOut {
    }

    @Test(expected = InvalidRecordException.class)
    public void testOut() throws Exception {
        manager.read("", BadMultipleDelimitedRecordOut.class);
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class FakeMultipleDelimitedRecordList {
        @SubRecord(type = "A")
        private FakeDelimitedRecordA recordA;
        @SubRecord(type = "B")
        private List<FakeDelimitedRecordB> recordB;

        public List<FakeDelimitedRecordB> getRecordB() {
            return recordB;
        }
    }

    @Test
    public void testList() throws Exception {
        FakeMultipleDelimitedRecordList record = manager.read(Common.delLineB, FakeMultipleDelimitedRecordList.class);
        Assert.assertEquals(1, record.getRecordB().size());
        manager.write(record);
    }

}
