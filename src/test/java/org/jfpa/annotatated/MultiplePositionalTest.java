package org.jfpa.annotatated;

import org.jfpa.annotation.MultiplePositional;
import org.jfpa.annotation.SubRecord;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
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
public class MultiplePositionalTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testReadA() throws Exception {
        FakeMultiplePositionalRecord record = manager.read(Common.posLineA, FakeMultiplePositionalRecord.class);
        Assert.assertNotNull(record.getRecordA());
        Assert.assertNull(record.getRecordB());
        Common.testRead(record.getRecordA());
    }

    @Test
    public void testReadB() throws Exception {
        FakeMultiplePositionalRecord record = manager.read(Common.posLineB, FakeMultiplePositionalRecord.class);
        Assert.assertNull(record.getRecordA());
        Assert.assertNotNull(record.getRecordB());
        Common.testRead(record.getRecordB());
    }

    @Test(expected = InvalidRecordException.class)
    public void testReadC() throws Exception {
        manager.read(Common.posLineC, FakeMultiplePositionalRecord.class);
    }

    @Test
    public void testWrite() throws Exception {
        FakeMultiplePositionalRecord record = new FakeMultiplePositionalRecord();
        record.setRecordA(manager.read(Common.posLineA, FakePositionalRecordA.class));
        Assert.assertEquals(Common.posLineA, manager.write(record));
    }

    @Test(expected = InvalidRecordException.class)
    public void testWriteFail() throws Exception {
        FakeMultiplePositionalRecord record = new FakeMultiplePositionalRecord();
        manager.write(record);
    }

    @Test
    public void testWriteFailBoth() throws Exception {
        FakeMultiplePositionalRecord record = new FakeMultiplePositionalRecord();
        record.setRecordA(manager.read(Common.posLineA, FakePositionalRecordA.class));
        record.setRecordB(manager.read(Common.posLineB, FakePositionalRecordB.class));
        Assert.assertEquals(Utility.buildNewLineString(Common.posLineA, Common.posLineB), manager.write(record));
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalRecord {
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
        @SubRecord(type = "B")
        private FakePositionalRecordB recordB;

        public FakePositionalRecordA getRecordA() {
            return recordA;
        }

        public void setRecordA(FakePositionalRecordA recordA) {
            this.recordA = recordA;
        }

        public FakePositionalRecordB getRecordB() {
            return recordB;
        }

        public void setRecordB(FakePositionalRecordB recordB) {
            this.recordB = recordB;
        }
    }

    @MultiplePositional
    public static class FakeMultiplePositionalRecordExtractor implements TypeExtractor {
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
        @SubRecord(type = "B")
        private FakePositionalRecordB recordB;

        public FakePositionalRecordA getRecordA() {
            return recordA;
        }

        public FakePositionalRecordB getRecordB() {
            return recordB;
        }

        public String extractType(String line) {
            return Utility.substring(line, 0, 1);
        }
    }

    @Test
    public void testExtractor() throws Exception {
        FakeMultiplePositionalRecordExtractor record = manager.read(Common.posLineA, FakeMultiplePositionalRecordExtractor.class);
        Assert.assertNotNull(record.getRecordA());
        Assert.assertNull(record.getRecordB());
        Common.testRead(record.getRecordA());
    }

    @MultiplePositional(typeExtractor = FakeMultiplePositionalRecordExtractor.class)
    public static class FakeMultiplePositionalRecordOuterExtractor {
    }

    @Test
    public void testOuterExtractor() throws Exception {
        manager.loadClass(FakeMultiplePositionalRecordOuterExtractor.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalRecordValidator implements MultipleRecordValidator {
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
        @SubRecord(type = "B")
        private List<FakePositionalRecordB> recordB;

        public void setRecordB(List<FakePositionalRecordB> recordB) {
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
        manager.read(Common.posLineA, FakeMultiplePositionalRecordValidator.class);
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testValidatorWrite() throws Exception {
        FakeMultiplePositionalRecordValidator record = new FakeMultiplePositionalRecordValidator();
        record.setRecordB(new ArrayList<FakePositionalRecordB>());
        manager.write(record);
    }

    @MultiplePositional
    public static class BadMultiplePositionalRecordNone {
    }

    @Test(expected = JfpaException.class)
    public void testFailNone() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordNone.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1, typeExtractor = BadMultiplePositionalRecordBoth.class)
    public static class BadMultiplePositionalRecordBoth implements TypeExtractor {
        public String extractType(String line) {
            return null;
        }
    }

    @Test(expected = JfpaException.class)
    public void testFailBoth() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordBoth.class);
    }

    @MultiplePositional(typePositionBegin = 20)
    public static class BadMultiplePositionalRecordEndMissing {}

    @Test(expected = JfpaException.class)
    public void testEndMissing() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordEndMissing.class);
    }

    @MultiplePositional(typePositionEnd = 20)
    public static class BadMultiplePositionalRecordBeginMissing {}

    @Test(expected = JfpaException.class)
    public void testBeginMissing() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordBeginMissing.class);
    }

    @MultiplePositional(typePositionBegin = 30, typePositionEnd = 20)
    public static class BadMultiplePositionalRecordBadNumbers {}

    @Test(expected = JfpaException.class)
    public void testBadNumbers() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordBadNumbers.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 0)
    public static class BadMultiplePositionalRecordNegativeNumbers {}

    @Test(expected = JfpaException.class)
    public void testNegativeNumbers() throws Exception {
        manager.loadClass(BadMultiplePositionalRecordNegativeNumbers.class);
    }

    @MultiplePositional(typePositionBegin = 10, typePositionEnd = 20)
    public static class BadMultiplePositionalRecordOut {}

    @Test(expected = InvalidRecordException.class)
    public void testOut() throws Exception {
        manager.read("", BadMultiplePositionalRecordOut.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalList {
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
        @SubRecord(type = "B")
        private List<FakePositionalRecordB> recordB;

        public List<FakePositionalRecordB> getRecordB() {
            return recordB;
        }
    }

    @Test
    public void testList() throws Exception {
        FakeMultiplePositionalList record = manager.read(Common.posLineB, FakeMultiplePositionalList.class);
        Assert.assertEquals(1, record.getRecordB().size());
        manager.write(record);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalNone {
        @SuppressWarnings("unused")
        private FakePositionalRecordA recordA;
    }

    @Test
    public void testNone() throws Exception {
        manager.loadClass(FakeMultiplePositionalNone.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalBadConstructor {
        private FakeMultiplePositionalBadConstructor() { }
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
    }

    @Test(expected = JfpaException.class)
    public void testBadConstructor() throws Exception {
        manager.read("", FakeMultiplePositionalBadConstructor.class);
    }
}
