package org.jfpa.inheritance;

import org.jfpa.dto.MultipleFlatRecordDTO;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.interfaces.MultipleFlatRecordDTOFactory;
import org.jfpa.record.AbstractMultipleVariableRecord;
import org.jfpa.record.AbstractPositionalVariableRecord;
import org.jfpa.type.MultipleRecordType;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 31/03/11
 */
public class MultipleVariableRecordTest {

    private FakePositionalVariableRecord header = new FakePositionalVariableRecord("520Type1");
    private FakePositionalVariableRecord header2 = new FakePositionalVariableRecord("520Type2");
    private FakePositionalVariableRecord headerFoo = new FakePositionalVariableRecord("520Foo  ");
    private FakePositionalVariableRecord normal = new FakePositionalVariableRecord("521Line2");
    private FakePositionalVariableRecord footer = new FakePositionalVariableRecord("599Line2");

    public MultipleVariableRecordTest() throws InvalidRecordException {

    }

    @Test
    public void test() throws Exception {
        MultipleFlatRecord record = new FakeAbstractMultipleVariableRecord();

        record.addRecord(header);
        record.addRecord(normal);
        record.addRecord(footer);

        Assert.assertEquals(FakeAbstractMultipleVariableRecord.T1, record.getType());

        FakeType1DTO dto1 = record.createDTO(FakeAbstractMultipleVariableRecord.factory1);
        Assert.assertNotNull(dto1);
        Assert.assertEquals("Line2", dto1.getValue());

        record.clear();

        record.addRecord(header2);
        record.addRecord(normal);
        record.addRecord(footer);

        FakeType2DTO dto2 = record.createDTO(FakeAbstractMultipleVariableRecord.factory2);
        Assert.assertNotNull(dto2);
        Assert.assertEquals("Line2", dto2.getValue());
    }

    @Test(expected = InvalidMultipleRecordException.class)
    public void testBad() throws Exception {
        MultipleFlatRecord record = new FakeAbstractMultipleVariableRecord();
        record.addRecord(footer);
        record.getType();
    }

    @Test
    public void testError() throws Exception {
        FakeAbstractMultipleVariableRecord record = new FakeAbstractMultipleVariableRecord();

        record.addRecord(headerFoo);
        record.addRecord(normal);
        record.addRecord(footer);

        Assert.assertFalse(record.isType(FakeAbstractMultipleVariableRecord.T1));
        Assert.assertFalse(record.isType(FakeAbstractMultipleVariableRecord.T2));
    }

    private static class FakePositionalVariableRecord extends AbstractPositionalVariableRecord {

        private static final int[] LENGTHS = new int[] {3,5};

        public static final RecordType HEADER_520 = new RecordType("520", LENGTHS);
        public static final RecordType NORMAL_521 = new RecordType("521", LENGTHS);
        public static final RecordType FOOTER_599 = new RecordType("599", LENGTHS);

        private static final Map<String, RecordType> map = RecordType.buildMap(HEADER_520, NORMAL_521, FOOTER_599);

        public FakePositionalVariableRecord(String record) throws InvalidRecordException {
            super(map, record);
        }

        public String extractType(String str) {
            return Utility.substring(str, 0, 3);
        }
    }

    private static class FakeAbstractMultipleVariableRecord extends AbstractMultipleVariableRecord {

        public static final MultipleRecordType T1 = new MultipleRecordType("Type1");
        public static final MultipleRecordType T2 = new MultipleRecordType("Type2");

        public static final MultipleFlatRecordDTOFactory<FakeType1DTO> factory1 = new MultipleFlatRecordDTOFactory<FakeType1DTO>() {
            public FakeType1DTO createDTO(MultipleFlatRecord record) {
                return new FakeType1DTO(record);
            }
        };
        public static final MultipleFlatRecordDTOFactory<FakeType2DTO> factory2 = new MultipleFlatRecordDTOFactory<FakeType2DTO>() {
            public FakeType2DTO createDTO(MultipleFlatRecord record) {
                return new FakeType2DTO(record);
            }
        };

        private static final Map<String, MultipleRecordType> map = MultipleRecordType.buildMap(T1,T2);

        protected FakeAbstractMultipleVariableRecord() {
            super(map);
        }

        public String extractType() {
            FlatRecord head = findFirstRecord(FakePositionalVariableRecord.HEADER_520);
            try {
                return head != null ? head.getString(1) : null;
            } catch (InvalidRecordException e) {
                return null;
            }
        }

        public boolean isFirst(FlatRecord record) {
            return record.isType(FakePositionalVariableRecord.HEADER_520);
        }

    }

    private static class FakeType1DTO extends MultipleFlatRecordDTO {

        public FakeType1DTO(MultipleFlatRecord record) {
            super(record);
        }

        public String getValue() {
            try {
                return record.findFirstRecord(FakePositionalVariableRecord.NORMAL_521).getString(1);
            } catch (InvalidRecordException e) {
                return null;
            }
        }

    }

    private static class FakeType2DTO extends MultipleFlatRecordDTO {

        public FakeType2DTO(MultipleFlatRecord record) {
            super(record);
        }

        public String getValue() {
            try {
                return record.findFirstRecord(FakePositionalVariableRecord.NORMAL_521).getString(1);
            } catch (InvalidRecordException e) {
                return null;
            }
        }

    }
}
