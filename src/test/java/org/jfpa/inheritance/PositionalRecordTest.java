package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.utility.Formats;
import org.jfpa.record.PositionalRecord;
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
 * Date: 22/03/11
 */
public class PositionalRecordTest {

    @Test
    public void testPositional() throws Exception {
        FlatRecord record = new FakePositionalRecord();
        String dateFormat = Formats.DATE_FORMAT;
        String[] booleanFormat = Formats.BOOLEAN_Y_N;
        Date date = Utility.stringToDate("22/03/2011", dateFormat);
        record.setString(0, "A");
        record.setString(1, "B");
        record.setString(2, "22/03/2011");
        Assert.assertEquals("A    B    22/03/2011 ", record.toString());
        Assert.assertEquals("A", record.getString(0));
        Assert.assertEquals("B", record.getString(1));
        Assert.assertEquals("22/03/2011", record.getString(2));
        Assert.assertEquals(date, record.getDate(2, dateFormat));
        Assert.assertNull(record.getBoolean(3, booleanFormat));
        record.setDate(2, date, dateFormat);
        Assert.assertEquals(date, record.getDate(2, dateFormat));
        record.setBigDecimal(2, BigDecimal.ZERO);
        record.setBoolean(3, true, booleanFormat);
        Assert.assertEquals("A    B    0         Y", record.toString());
        Assert.assertEquals(BigDecimal.ZERO, record.getBigDecimal(2));
        record.setLong(2, 0L);
        record.setBoolean(3, false, booleanFormat);
        Assert.assertEquals("A    B    0         N", record.toString());
        Assert.assertEquals(new Long(0L), record.getLong(2));
        record = new FakePositionalRecord(Utility.rightPad("A", 21));
        Assert.assertEquals("A                    ", record.toString());
        record.validate();
        Assert.assertFalse(record.isType());
    }

    @Test
    public void testDto() throws Exception {
        FlatRecord record = new FakePositionalRecord(Utility.rightPad("Teo", 21));
        FakeDTO dto = record.createDTO(FakePositionalRecord.DEFAULT);
        Assert.assertEquals("Teo                  ", record.toString());
        Assert.assertEquals("Teo", dto.getNome());
        dto.setNome("None");
        Assert.assertFalse("Teo".equals(dto.getNome()));
        Assert.assertEquals("None                 ", record.toString());
    }

    @Test(expected = InvalidRecordException.class)
    public void testInvalidString() throws Exception {
        new FakePositionalRecord("");
    }

    @Test(expected = InvalidRecordException.class)
    public void testTooShort() throws Exception {
        new FakePositionalRecord(" ");
    }

    @Test(expected = InvalidRecordException.class)
    public void testValueTooLong() throws Exception {
        FlatRecord record = new FakePositionalRecord();
        record.setString(0, "123456");
    }

    private static class FakePositionalRecord extends PositionalRecord {

        private static final int[] LENGTHS = new int[]{5, 5, 10, 1};

        private static final FlatRecordDTOFactory<FakeDTO> factory = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(LENGTHS, factory);

        public FakePositionalRecord() {
            super(DEFAULT);
        }

        public FakePositionalRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    private static class FakeDTO extends FlatRecordDTO {

        public FakeDTO(FlatRecord record) {
            super(record);
        }

        public String getNome() throws InvalidRecordException {
            return record.getString(0);
        }

        public void setNome(String nome) throws InvalidRecordException {
            record.setString(0, nome);
        }
    }

    private static class FakePositionalRecordBad extends PositionalRecord {

        private static final RecordType DEFAULT = new RecordType(new int[]{5, 0, 10});

        private FakePositionalRecordBad() {
            super(FakePositionalRecordBad.DEFAULT);
        }
    }

    @Test(expected = ExceptionInInitializerError.class)
    public void testBad() throws Exception {
        new FakePositionalRecordBad();
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadPos() throws Exception {
        FakePositionalRecord record = new FakePositionalRecord(Utility.spaces(30));
        Assert.assertEquals(4, record.getColumns());
        record.getString(13);
    }
}
