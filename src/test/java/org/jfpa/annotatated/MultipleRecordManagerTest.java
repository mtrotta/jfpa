package org.jfpa.annotatated;

import org.jfpa.annotation.*;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.MultipleRecordValidator;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.manager.MultipleRecordManager;
import org.jfpa.manager.RecordManager;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 11/10/11
 */
public class MultipleRecordManagerTest {

    private static final String lineF = "FVal1 ";
    private static final String lineN = "NVal2 ";

    private boolean complete;

    private RecordHandler<FakeMultipleRecord> handler = new RecordHandler<FakeMultipleRecord>() {
        public void handle(FakeMultipleRecord record) {
            FirstRecord first = record.getFirst();
            NormalRecord normal = record.getNormal();
            Assert.assertNotNull(first);
            Assert.assertNotNull(normal);
            Assert.assertTrue(Utility.containsAllValues(first, normal));
            Assert.assertTrue(Utility.containsAnyValues(first, normal));
            Assert.assertEquals("F", first.getType());
            Assert.assertEquals("N", normal.getType());
            Assert.assertEquals("Val1", first.getValue());
            Assert.assertEquals("Val2", normal.getValue());
            complete = true;
        }
    };

    @Before
    public void setUp() throws Exception {
        complete = false;
    }

    @Test
    public void testIncomplete() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecord.class, handler);
        multipleRecordManager.flush();
        Assert.assertFalse(complete);
    }

    @Test
    public void testComplete() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecord.class, handler);
        multipleRecordManager.read(lineF);
        multipleRecordManager.read(lineN);
        multipleRecordManager.flush();
        Assert.assertTrue(complete);
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testOutOfSync() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecord.class, handler);
        multipleRecordManager.read(lineN);
    }

    @Positional
    public static class FirstRecord {
        @TextColumn(length = 1)
        private String type;
        @TextColumn(length = 5)
        private String value;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    @Positional
    public static class NormalRecord {
        @TextColumn(length = 1)
        private String type;
        @TextColumn(length = 5)
        private String value;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecord {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private NormalRecord normal;

        public FirstRecord getFirst() {
            return first;
        }

        public NormalRecord getNormal() {
            return normal;
        }
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordList {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private List<NormalRecord> normal;

        public FirstRecord getFirst() {
            return first;
        }

        public List<NormalRecord> getNormal() {
            return normal;
        }
    }

    private RecordHandler<FakeMultipleRecordList> handlerList = new RecordHandler<FakeMultipleRecordList>() {
        public void handle(FakeMultipleRecordList record) {
            FirstRecord first = record.getFirst();
            List<NormalRecord> normal = record.getNormal();
            Assert.assertNotNull(first);
            Assert.assertNotNull(normal);
            Assert.assertEquals("F", first.getType());
            Assert.assertEquals(3, normal.size());
            complete = true;
        }
    };

    @Test
    public void testList() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecordList.class, handlerList);
        multipleRecordManager.read(lineF);
        multipleRecordManager.read(lineN);
        multipleRecordManager.read(lineN);
        multipleRecordManager.read(lineN);
        multipleRecordManager.flush();
        Assert.assertTrue(complete);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordListBad {
        @SubRecord(type = "F")
        private List<FirstRecord> first;
        @SubRecord(type = "N")
        private List<NormalRecord> normal;
    }

    @Test(expected = JfpaException.class)
    public void testListBadFirst() throws Exception {
        new MultipleRecordManager(FakeMultipleRecordListBad.class, null);
    }

    @Test(expected = JfpaException.class)
    public void testBad() throws Exception {
        new MultipleRecordManager(BadClass.class, null);
    }

    @Positional
    public static class BadClass {
    }

    @MultiplePositional
    @MultipleDelimited
    public static class BadClassBoth {
    }

    @Test(expected = JfpaException.class)
    public void testBoth() throws Exception {
        new MultipleRecordManager(BadClassBoth.class, null);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordValidator implements MultipleRecordValidator {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private List<NormalRecord> normal = null;

        public void validate() throws InvalidMultipleRecordException {
            if (normal == null || normal.size() < 3)
                throw new InvalidMultipleRecordException("not enough normal records");
        }
    }

    private RecordHandler<FakeMultipleRecordValidator> handlerValidator = new RecordHandler<FakeMultipleRecordValidator>() {
        public void handle(FakeMultipleRecordValidator record) {
            complete = true;
        }
    };

    @Test
    public void testValidator() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecordValidator.class, handlerValidator);
        multipleRecordManager.read(lineF);
        multipleRecordManager.read(lineN);
        multipleRecordManager.read(lineN);
        multipleRecordManager.read(lineN);
        multipleRecordManager.flush();
        Assert.assertTrue(complete);
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testValidatorFail() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecordValidator.class, handlerValidator);
        multipleRecordManager.read(lineF);
        multipleRecordManager.read(lineN);
        multipleRecordManager.read(lineN);
        multipleRecordManager.flush();
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordBadMultiple {
        @SubRecord(type = "F")
        private FakeMultipleRecord first;
        @SubRecord(type = "N")
        private NormalRecord normal;
    }

    @Test(expected = JfpaException.class)
    public void testBadMultiple() throws Exception {
        new MultipleRecordManager(FakeMultipleRecordBadMultiple.class, null);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordManyNormal {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private NormalRecord normal;
        @SubRecord(type = "P")
        private NormalRecord normalP;
        @SubRecord(type = "Q")
        private NormalRecord normalQ;

        public NormalRecord getNormalP() {
            return normalP;
        }

        public NormalRecord getNormalQ() {
            return normalQ;
        }
    }

    @Test
    public void testManyNormal() throws Exception {
        MultipleRecordManager recordManager = new MultipleRecordManager(FakeMultipleRecordManyNormal.class, new RecordHandler<FakeMultipleRecordManyNormal>() {
            public void handle(FakeMultipleRecordManyNormal record) throws InvalidRecordException {
                Assert.assertEquals("Val3", record.getNormalP().getValue());
                Assert.assertEquals("Val4", record.getNormalQ().getValue());
            }
        });
        recordManager.read(lineF);
        recordManager.read(lineN);
        recordManager.read("PVal3 ");
        recordManager.read("QVal4 ");
        recordManager.flush();
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class BadConstructor {
        private BadConstructor() {}
        @SubRecord(type = "F")
        private FirstRecord first;
    }

    @Test(expected = JfpaException.class)
    public void testBadConstructor() throws Exception {
        MultipleRecordManager recordManager = new MultipleRecordManager(BadConstructor.class, null);
        recordManager.read(lineF);
        recordManager.flush();
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public abstract class BadAbstract {
        @SubRecord(type = "F")
        private FirstRecord first;
    }

    @Test(expected = JfpaException.class)
    public void testBadAbstract() throws Exception {
        MultipleRecordManager recordManager = new MultipleRecordManager(BadAbstract.class, null);
        recordManager.read(lineF);
        recordManager.flush();
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordTwoFirst {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N", first = true)
        private NormalRecord normal;

        public FirstRecord getFirst() {
            return first;
        }

        public NormalRecord getNormal() {
            return normal;
        }
    }

    private RecordHandler<FakeMultipleRecordTwoFirst> handlerTwoFirst = new RecordHandler<FakeMultipleRecordTwoFirst>() {
        public void handle(FakeMultipleRecordTwoFirst record) {
            Assert.assertTrue(record.getFirst() != null || record.getNormal() != null);
        }
    };

    @Test
    public void testTwoFirst() throws Exception {
        MultipleRecordManager multipleRecordManager = new MultipleRecordManager(FakeMultipleRecordTwoFirst.class, handlerTwoFirst);
        multipleRecordManager.read(lineF);
        multipleRecordManager.read(lineN);
        multipleRecordManager.flush();
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordRawList {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private List normal;


    }

    @Test(expected = JfpaException.class)
    public void testRawList() throws Exception {
        RecordManager recordManager = new RecordManager();
        recordManager.loadClass(FakeMultipleRecordRawList.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordWildList {
        @SubRecord(type = "F")
        private FirstRecord first;
        @SubRecord(type = "N")
        private List<?> normal;


    }

    @Test(expected = JfpaException.class)
    public void testWildList() throws Exception {
        RecordManager recordManager = new RecordManager();
        recordManager.loadClass(FakeMultipleRecordWildList.class);
    }
}
