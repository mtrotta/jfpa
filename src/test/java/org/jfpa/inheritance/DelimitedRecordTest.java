package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.utility.Formats;
import org.jfpa.record.DelimitedRecord;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 23/03/11
 */
public class DelimitedRecordTest {

    @Test
    public void testBasic() throws Exception {
        RecordType recordType = new RecordType(";", 3);
        DelimitedRecord recordWrite = new DelimitedRecord(recordType);
        Assert.assertEquals(";;", recordWrite.toString());
        DelimitedRecord recordRead = new DelimitedRecord(recordType, "A;B;C");
        Assert.assertEquals("A", recordRead.getString(0));
        Assert.assertEquals("B", recordRead.getString(1));
        Assert.assertEquals("C", recordRead.getString(2));
    }

    @Test
    public void testDelimited() throws Exception {
        final String dateFormat = Formats.DATE_FORMAT;
        final String[] booleanFormat = Formats.BOOLEAN_Y_N;
        FlatRecord record = new FakeDelimitedRecord();
        Date date = Utility.stringToDate("22/03/2011", dateFormat);
        record.setString(0, "A");
        record.setString(1, "B");
        record.setString(2, "22/03/2011");
        Assert.assertEquals("A;B;22/03/2011;;;;;;;", record.toString());
        Assert.assertEquals("A", record.getString(0));
        Assert.assertEquals("B", record.getString(1));
        Assert.assertEquals("22/03/2011", record.getString(2));
        Assert.assertEquals(date, record.getDate(2, dateFormat));
        Assert.assertNull(record.getBoolean(9, booleanFormat));
        record.setDate(2, date, dateFormat);
        Assert.assertEquals(date, record.getDate(2, dateFormat));
        record.setBigDecimal(2, BigDecimal.ZERO);
        record.setBoolean(9, true, booleanFormat);
        Assert.assertEquals("A;B;0;;;;;;;Y", record.toString());
        Assert.assertEquals(BigDecimal.ZERO, record.getBigDecimal(2));
        record.setLong(2, 0L);
        record.setBoolean(9, false, booleanFormat);
        Assert.assertEquals("A;B;0;;;;;;;N", record.toString());
        Assert.assertEquals(new Long(0L), record.getLong(2));
        record = new FakeDelimitedRecord("A;B;22/03/2011;;;;;;;");
        Assert.assertEquals("A;B;22/03/2011;;;;;;;", record.toString());
        Assert.assertEquals("A", record.getString(0));
        Assert.assertEquals("B", record.getString(1));
        Assert.assertEquals(date, record.getDate(2, dateFormat));
        record.validate();
    }

    public static class FakeDelimitedRecord extends DelimitedRecord {

        private static final int NUMBER = 10;
        private static final String DELIMITER = ";";

        public static final RecordType DEFAULT = new RecordType(DELIMITER, NUMBER);

        public FakeDelimitedRecord() {
            super(DEFAULT);
        }

        public FakeDelimitedRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    public static class Dto extends FlatRecordDTO {
        public Dto(FlatRecord record) {
            super(record);
        }
        public String getValue() throws InvalidRecordException {
            return record.getString(0);
        }
    }

    public static class FakeDelimitedRecordDto extends DelimitedRecord {

        private static final int NUMBER = 1;
        private static final String DELIMITER = ";";

        public static final RecordTypeDTO<Dto> DEFAULT = new RecordTypeDTO<Dto>(DELIMITER, NUMBER, new FlatRecordDTOFactory<Dto>() {
            public Dto createDTO(FlatRecord record) {
                return new Dto(record);
            }
        });

        public FakeDelimitedRecordDto(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    @Test
    public void testDto() throws Exception {
        FakeDelimitedRecordDto record = new FakeDelimitedRecordDto("Test");
        Dto dto = record.createDTO(FakeDelimitedRecordDto.DEFAULT);
        Assert.assertEquals("Test", record.toString());
        Assert.assertEquals("Test", dto.getValue());
    }

    public static class FakeDelimitedLengthRecord extends DelimitedRecord {

        private static final int NUMBER = 2;
        private static final String DELIMITER = ";";

        public static final RecordType DEFAULT = new RecordType("T", DELIMITER, NUMBER, new int[]{5,5});

        public FakeDelimitedLengthRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    @Test
    public void testLength() throws Exception {
        new FakeDelimitedLengthRecord("123456;12345");
    }

    public static class FakeDelimitedRecordEnclosed extends DelimitedRecord {

        private static final int NUMBER = 2;
        private static final String DELIMITER = ";";
        private static final String ENCLOSE = "\"";

        public static final RecordType DEFAULT = new RecordType(DELIMITER, NUMBER, ENCLOSE);

        public FakeDelimitedRecordEnclosed() {
            super(DEFAULT);
        }

        public FakeDelimitedRecordEnclosed(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    @Test
    public void testEnclose() throws Exception {
        FlatRecord record = new FakeDelimitedRecordEnclosed("\"123456\";\"12345\"");
        Assert.assertEquals("123456", record.getString(0));
    }

    public static class FakeDelimitedRecordEnclosedLength extends DelimitedRecord {

        private static final int NUMBER = 2;
        private static final String DELIMITER = ";";
        private static final String ENCLOSE = "\"";

        public static final RecordType DEFAULT = new RecordType(DELIMITER, NUMBER, ENCLOSE, new int[]{5,5});

        public FakeDelimitedRecordEnclosedLength() {
            super(DEFAULT);
        }

        public FakeDelimitedRecordEnclosedLength(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    @Test(expected = InvalidRecordException.class)
    public void testEncloseLength() throws Exception {
        FlatRecord record = new FakeDelimitedRecordEnclosedLength("\"123456\";\"12345\"");
        record.getString(0);
    }

    public static class FakeDelimitedRecordEnclosedDto extends DelimitedRecord {

        private static final int NUMBER = 1;
        private static final String DELIMITER = ";";

        public static final RecordTypeDTO<Dto> DEFAULT = new RecordTypeDTO<Dto>(DELIMITER, NUMBER, "\"", new FlatRecordDTOFactory<Dto>() {
            public Dto createDTO(FlatRecord record) {
                return new Dto(record);
            }
        });

        public FakeDelimitedRecordEnclosedDto(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    @Test
    public void testEnclosedDto() throws Exception {
        FakeDelimitedRecordEnclosedDto record = new FakeDelimitedRecordEnclosedDto("\"Test\"");
        Dto dto = record.createDTO(FakeDelimitedRecordEnclosedDto.DEFAULT);
        Assert.assertEquals("Test", dto.getValue());
    }
}
