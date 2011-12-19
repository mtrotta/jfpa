package org.jfpa.inheritance;

import org.jfpa.builder.MultipleRecordBuilder;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.record.AbstractDelimitedVariableRecord;
import org.jfpa.record.AbstractMultipleRecord;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 24/03/11
 */
public class MultipleRecordBuilderTest {

    private final FakeAbstractDelimitedRecord header;
    private final FakeAbstractDelimitedRecord normal;
    private final FakeAbstractDelimitedRecord footer;

    private boolean complete;

    public MultipleRecordBuilderTest() throws InvalidRecordException {
        header = new FakeAbstractDelimitedRecord("AAA:111");
        normal = new FakeAbstractDelimitedRecord("111:111");
        footer = new FakeAbstractDelimitedRecord("ZZZ:111");
    }

    @Before
    public void before() throws Exception {
        complete = false;
    }

    @Test
    public void test() throws Exception {
        MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
        builder.add(header);
        builder.add(normal);
        builder.add(footer);
        builder.flush();
        Assert.assertTrue(complete);
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testOutOfSync() throws Exception {
        MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
        builder.add(normal);
    }

    @Test
    public void testIncomplete() throws Exception  {
        try {
            MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
            builder.add(header);
            builder.add(normal);
            builder.flush();
            Assert.fail();
        } catch (InvalidMultipleRecordException e) {
            Assert.assertEquals(Utility.buildNewLineString(Arrays.asList(header, normal)), e.getRecord());
        }
    }

    @Test
    public void testIncompleteHandled() throws Exception {
        MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
        builder.add(header);
        try {
            builder.flush();
            Assert.fail();
        } catch (InvalidMultipleRecordException e) {}
        builder.add(header);
        builder.add(normal);
        builder.add(footer);
        builder.flush();
    }

    private RecordHandler<FakeMultipleRecord> getHandler() {
        return new RecordHandler<FakeMultipleRecord>() {
            public void handle(FakeMultipleRecord record) {
                complete = true;
            }
        };
    }

    @Test
    public void testDelimited() throws Exception {
        FakeAbstractDelimitedRecord header = new FakeAbstractDelimitedRecord("AAA:111");
        Assert.assertEquals("111", header.getValue());
        FakeAbstractDelimitedRecord normal = new FakeAbstractDelimitedRecord("111:222");
        Assert.assertEquals("222", normal.getValue());
        FakeAbstractDelimitedRecord footer = new FakeAbstractDelimitedRecord("ZZZ:222");
        Assert.assertEquals("222", footer.getValue());
        MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
        builder.add(header);
        builder.add(normal);
        Assert.assertFalse(complete);
        builder.add(footer);
        builder.flush();
        Assert.assertTrue(complete);
        FakeAbstractDelimitedRecord rec = new FakeAbstractDelimitedRecord("AAA:");
        Assert.assertNull("111", rec.getValue());
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testInvalid() throws Exception {
        FakeAbstractDelimitedRecord header = new FakeAbstractDelimitedRecord("AAA:111");
        FakeAbstractDelimitedRecord footer = new FakeAbstractDelimitedRecord("ZZZ:222");
        MultipleRecordBuilder<FakeMultipleRecord> builder = new MultipleRecordBuilder<FakeMultipleRecord>(new FakeMultipleRecord(), getHandler());
        builder.add(header);
        builder.add(footer);
        builder.flush();
    }

    private static class FakeAbstractDelimitedRecord extends AbstractDelimitedVariableRecord {

        private static final String DELIMITER = ":";

        public static final RecordType HEADER = new RecordType("AAA", DELIMITER, 2);
        public static final RecordType NORMAL = new RecordType("111", DELIMITER, 2);
        public static final RecordType FOOTER = new RecordType("ZZZ", DELIMITER, 2);

        private static final Map<String, RecordType> map = RecordType.buildMap(HEADER,NORMAL,FOOTER);

        public FakeAbstractDelimitedRecord(String str) throws InvalidRecordException {
            super(map, str);
        }

        public String getValue() throws InvalidRecordException {
            return getColumns() > 1 ? getString(1) : null;
        }

        public String extractType(String str) {
            return Utility.substring(str, 0, 3);
        }
    }

    private static class FakeMultipleRecord extends AbstractMultipleRecord {

        public boolean isFirst(FlatRecord record) {
            return record.isType(FakeAbstractDelimitedRecord.HEADER);
        }

        @Override
        public boolean isComplete() {
            return contains(FakeAbstractDelimitedRecord.FOOTER);
        }

        @Override
        public void validate() throws InvalidMultipleRecordException {
            super.validate();
            if (!contains(FakeAbstractDelimitedRecord.NORMAL))
                invalidateRecord("doesn't contain NORMAL");
        }
    }
}
